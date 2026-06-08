/*
 * (C) Copyright 2026 Hyland (https://hyland.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.ingest;

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;
import org.hyland.sdk.cic.http.client.util.StringUtils;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

/**
 * HTTP client for interacting with the CIC Ingest service.
 * 
 * @since 1.0.0
 */
public class IngestHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String INGESTION_EVENTS_PATH = "/v2/ingestion-events";

    private static final String PRESIGNED_URLS_PATH = "/v1/presigned-urls";

    private static final String CHECK_DIGEST_PATH = "/v1/check-digest";

    protected final String sourceId;

    private final int presignedUrlsCount;

    // ---------------
    // Instantiation
    // ---------------

    protected IngestHttpClient(Builder builder) {
        super(builder);
        this.sourceId = builder.sourceId;
        this.presignedUrlsCount = builder.presignedUrlsCount;
    }

    public static Builder from() {
        // TODO turn this to production
        return from("https://ingestion.insight.dev.experience.hyland.com");
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    // --------------
    // Service APIs
    // --------------

    /**
     * Checks if a document already contains a blob with the given digest.
     *
     * @param sourceId the content source identifier
     * @param objectId the content identifier in the source repository
     * @param digest the blob digest
     * @return true if the blob exists, false if not found (404) or exists=false in response
     * @throws CICSdkException if the request fails or returns an unexpected status code (400, 401, 403, 500)
     */
    public boolean checkDigest(String sourceId, String objectId, String digest) {
        sourceId = sourceId == null ? this.sourceId : sourceId;
        StringUtils.requireNonBlank(sourceId, "sourceId must be provided either in method parameter or builder");
        var request = this.requestBuilder(GET, CHECK_DIGEST_PATH + "/" + sourceId + "/" + objectId)
                          .queryParameter("digest", digest)
                          .build();

        var response = sendThenReadAsString(request);
        int statusCode = response.statusCode();

        if (statusCode == 200) {
            var cicObject = MapperService.read(response.body(), CICObject.class);
            return cicObject.getBoolean("exists", false);
        } else if (statusCode == 404) {
            return false;
        } else if (ErrorUtils.isUnexpectedStatusCode(statusCode)) {
            ErrorUtils.throwException(response,
                    "Failed to check digest, HTTP response returned with status code: " + statusCode);
            return false;
        } else {
            return false;
        }
    }

    /**
     * Sends an ingest event to the CIC Ingest service.
     *
     * @param event the ingest event to send
     * @throws CICSdkException if the request fails or returns a non-202 status code
     */
    public void ingest(IngestEvent event) {
        var request = this.requestBuilder(POST, INGESTION_EVENTS_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(IngestEvent.Batch.of(
                                  event.toBuilder().sourceId(event.sourceId().orElse(this.sourceId)).build())))
                          .build();

        var response = sendThenReadAsString(request);
        int statusCode = response.statusCode();
        if (statusCode != 202) {
            ErrorUtils.throwException(response,
                    "Couldn't ingest the document, HTTP response returned with status code: " + statusCode);
        }
    }

    /**
     * Retrieves a list of pre-signed URLs for blob upload.
     *
     * @return a list of {@link PreSignedUrl}
     */
    public List<PreSignedUrl> retrievePreSignedUrls() {
        var request = this.requestBuilder(POST, PRESIGNED_URLS_PATH)
                          .queryParameter("count", String.valueOf(presignedUrlsCount))
                          .build();
        return sendThenMapAs(request, PreSignedUrl.List.class);
    }

    /**
     * Uploads a blob to the given pre-signed URL with retry support.
     * <p>
     * Retries are governed by the {@link org.hyland.sdk.cic.http.client.retry.RetryPolicy} configured on this client.
     * The upload uses PUT (idempotent), so it is safe to retry on transient failures.
     * <p>
     *
     * @param preSignedUrl the pre-signed URL
     * @param blob the blob to upload
     * @throws CICSdkException if upload fails after all retry attempts or is interrupted
     */
    public void upload(String preSignedUrl, CICBlob blob) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("cic-upload-", ".tmp");
            try (var is = blob.getInputStream()) {
                Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            Path finalTempFile = tempFile;
            var response = sendRawWithRetry(() -> {
                try {
                    return HttpRequest.newBuilder(URI.create(preSignedUrl))
                                      .PUT(HttpRequest.BodyPublishers.ofFile(finalTempFile))
                                      .header("Content-Type", blob.getContentType().orElse("application/octet-stream"))
                                      .build();
                } catch (IOException e) {
                    throw new CICSdkException("Failed to build upload request", e);
                }
            }, "PUT", HttpResponse.BodyHandlers.ofString(), statusCode -> statusCode == 200);
            if (response.statusCode() != 200) {
                throw new CICSdkException(
                        "Couldn't upload the blob, HTTP response returned with status code: " + response.statusCode());
            }
        } catch (IOException e) {
            throw new CICSdkException("Failed to buffer blob for upload", e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    // ignore cleanup failure
                }
            }
        }
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, IngestHttpClient> {

        protected String sourceId;

        private int presignedUrlsCount = 100;

        /**
         * Creates a builder for IngestHttpClient.
         *
         * @param baseUrl the base URL of the CIC Ingest service
         * @param authenticationBuilder the authentication builder
         */
        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        /**
         * Sets the hxp-environment header.
         *
         * @param environment the environment value
         * @return this builder
         */
        public Builder hxpEnvironment(String environment) {
            return header("hxp-environment", environment);
        }

        /**
         * Sets the sourceId in REST URLs or Body.
         *
         * @param sourceId the source id
         * @return this builder
         */
        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        /**
         * Sets the number of pre-signed URLs to retrieve in a single request.
         */
        public Builder presignedUrlsCount(int count) {
            this.presignedUrlsCount = count;
            return this;
        }

        @Override
        public IngestHttpClient build() {
            return new IngestHttpClient(this);
        }
    }

}
