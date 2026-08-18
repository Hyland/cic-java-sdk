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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke;

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;
import org.hyland.sdk.cic.ke.object.ProcessResponse;

/**
 * HTTP client for interacting with the CIC Context (Knowledge Enrichment) API.
 *
 * @since 1.0.0
 */
public class KEHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String PRESIGNED_URL_PATH = "/files/upload/presigned-url";

    private static final String PROCESS_PATH = "/content/process";

    private static final String ACTIONS_PATH = "/content/actions";

    private static final String HEALTHY_PATH = "/healthy";

    // ---------------
    // Instantiation
    // ---------------

    protected KEHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    // ---------------
    // Pipeline APIs
    // ---------------

    /**
     * Gets a presigned URL for uploading a document file.
     *
     * @param contentType the content type of the file (e.g., "image/jpeg", "application/pdf")
     * @return the presigned URL and object key
     * @throws CICSdkException if the request fails
     */
    public PresignedUrl getPresignedUrl(String contentType) {
        var request = this.requestBuilder(GET, PRESIGNED_URL_PATH).queryParameter("contentType", contentType).build();
        return sendThenMapAs(request, PresignedUrl.class);
    }

    /**
     * Uploads a blob to the given presigned URL.
     *
     * @param presignedUrl the presigned upload URL
     * @param blob the blob to upload
     * @throws CICSdkException if upload fails
     */
    public void upload(String presignedUrl, CICBlob blob) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("cic-ke-upload-", ".tmp");
            try (var is = blob.getInputStream()) {
                Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            Path finalTempFile = tempFile;
            var response = sendRawWithRetry(() -> {
                try {
                    var uploadRequest = HttpRequest.newBuilder(URI.create(presignedUrl))
                                                   .PUT(HttpRequest.BodyPublishers.ofFile(finalTempFile))
                                                   .header("Content-Type",
                                                           blob.getContentType().orElse("application/octet-stream"));
                    if (requestTimeout != null) {
                        uploadRequest.timeout(requestTimeout);
                    }
                    return uploadRequest.build();
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

    /**
     * Requests enrichment process execution for selected actions and documents.
     *
     * @param processRequest the process request with object keys and actions
     * @return the processing ID as a string
     * @throws CICSdkException if the request fails
     */
    public String process(ProcessRequest processRequest) {
        var request = this.requestBuilder(POST, PROCESS_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(processRequest))
                          .build();
        var response = sendThenMapAs(request, ProcessResponse.class);
        return response.processingId();
    }

    /**
     * Retrieves the enrichment results for a processing job.
     *
     * @param processingId the processing identifier
     * @return the enrichment result
     * @throws CICSdkException if the request fails
     */
    public EnrichmentResult getResults(String processingId) {
        var request = this.requestBuilder(GET, PROCESS_PATH + "/" + encodePathSegment(processingId) + "/results")
                          .build();
        return sendThenMapAs(request, EnrichmentResult.class);
    }

    /**
     * Retrieves the enrichment results, returning null if still processing (HTTP 202).
     *
     * @param processingId the processing identifier
     * @return the enrichment result, or null if still processing
     * @throws CICSdkException if the request fails with an unexpected status code
     */
    public EnrichmentResult getResultsIfReady(String processingId) {
        var request = this.requestBuilder(GET, PROCESS_PATH + "/" + encodePathSegment(processingId) + "/results")
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() == 202) {
            return null;
        }
        org.hyland.sdk.cic.http.client.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
        return org.hyland.sdk.cic.http.client.mapper.MapperService.read(response.body(), EnrichmentResult.class);
    }

    /**
     * Lists available enrichment actions.
     *
     * @return the actions response body as a string
     * @throws CICSdkException if the request fails
     */
    public String getActions() {
        var request = this.requestBuilder(GET, ACTIONS_PATH).build();
        var response = sendThenReadAsString(request);
        org.hyland.sdk.cic.http.client.util.ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
        return response.body();
    }

    /**
     * Checks availability of the Context Service.
     *
     * @return true if the service is healthy
     */
    public boolean isHealthy() {
        var response = sendThenReadAsString(this.requestBuilder(GET, HEALTHY_PATH).build());
        return response.statusCode() == 200;
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, KEHttpClient> {

        /**
         * Creates a builder for KEHttpClient.
         *
         * @param baseUrl the base URL of the Context (KE) API
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

        @Override
        public KEHttpClient build() {
            return new KEHttpClient(this);
        }
    }
}
