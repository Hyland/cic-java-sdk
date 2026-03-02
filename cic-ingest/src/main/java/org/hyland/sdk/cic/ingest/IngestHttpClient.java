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
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

/**
 * HTTP client for interacting with the CIC Ingest service.
 * 
 * @since 1.0.0
 */
public class IngestHttpClient extends AbstractAuthenticatedHttpClient {

    // ---------------
    // Instantiation
    // ---------------

    protected IngestHttpClient(Builder builder) {
        super(builder);
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
     * @param documentId the document identifier
     * @param digest the blob digest
     * @return true if the blob exists, false otherwise
     * @throws CICSdkException if the request fails or returns an unexpected status code
     */
    public boolean checkDigest(String documentId, String digest) {
        var request = this.requestBuilder(GET, "/v1/check-digest/" + documentId)
                          .queryParameter("digest", digest)
                          .build();

        var response = sendThenReadAsString(request);
        if (ErrorUtils.isUnexpectedStatusCode(response.statusCode())) {
            return false;
        } else {
            var cicObject = MapperService.read(response.body(), CICObject.class);
            return cicObject.getBoolean("exists", false);
        }
    }

    /**
     * Sends an ingest event to the CIC Ingest service.
     *
     * @param event the ingest event to send
     * @throws CICSdkException if the request fails or returns a non-202 status code
     */
    public void ingest(IngestEvent event) {
        var request = this.requestBuilder(POST, "/v2/ingestion-events")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(event))
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
        var request = this.requestBuilder(POST, "/v1/presigned-urls")
                          .queryParameter("count", "100") // TODO make it configurable
                          .build();
        return sendThenMapAs(request, PreSignedUrl.List.class);
    }

    /**
     * Uploads a blob to the given pre-signed URL.
     *
     * @param preSignedUrl the pre-signed URL
     * @param blob the blob to upload
     * @throws CICSdkException if upload fails or is interrupted
     */
    public void upload(String preSignedUrl, CICBlob blob) {
        try {
            // presigned url could be anywhere, so build a request from the ground
            var jdkRequest = HttpRequest.newBuilder(URI.create(preSignedUrl))
                                        .POST(HttpRequest.BodyPublishers.ofInputStream(blob::getInputStream))
                                        .build();
            var jdkResponse = client.send(jdkRequest, HttpResponse.BodyHandlers.ofString());
            if (jdkResponse.statusCode() != 200) {
                throw new CICSdkException("Couldn't upload the blob, HTTP response returned with status code: "
                        + jdkResponse.statusCode());
            }
        } catch (IOException e) {
            throw new CICSdkException("An error occurred during request execution", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CICSdkException("Interrupted while sending the request", e);
        }
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, IngestHttpClient> {

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

        @Override
        public IngestHttpClient build() {
            return new IngestHttpClient(this);
        }
    }

}
