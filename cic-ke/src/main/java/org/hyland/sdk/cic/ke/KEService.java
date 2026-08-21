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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ActionDescriptor;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * High-level service for the Context (Knowledge Enrichment) API providing end-to-end workflows.
 *
 * @since 1.0.0
 */
public class KEService {

    protected final KEHttpClient httpClient;

    private int pollMaxAttempts = 20;

    private long pollIntervalMs = 5000;

    public KEService(KEHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Configures the polling behavior for the {@link #enrich} methods.
     *
     * @param maxAttempts the maximum number of polling attempts
     * @param intervalMs the sleep interval between polls in milliseconds
     */
    public void setPollSettings(int maxAttempts, long intervalMs) {
        this.pollMaxAttempts = maxAttempts;
        this.pollIntervalMs = intervalMs;
    }

    /**
     * Gets a presigned URL for uploading a document file.
     *
     * @param contentType the MIME content type of the file
     * @return the presigned URL and object key
     * @throws CICSdkException if the request fails
     */
    public PresignedUrl getPresignedUrl(String contentType) {
        Objects.requireNonNull(contentType, "contentType cannot be null");
        return httpClient.getPresignedUrl(contentType);
    }

    /**
     * Uploads a blob to the given presigned URL.
     *
     * @param presignedUrl the presigned upload URL
     * @param blob the blob to upload
     * @throws CICSdkException if upload fails
     */
    public void upload(String presignedUrl, CICBlob blob) {
        Objects.requireNonNull(presignedUrl, "presignedUrl cannot be null");
        Objects.requireNonNull(blob, "blob cannot be null");
        httpClient.upload(presignedUrl, blob);
    }

    /**
     * Submits a process request for enrichment.
     *
     * @param processRequest the process request
     * @return the processing ID
     * @throws CICSdkException if the request fails
     */
    public String process(ProcessRequest processRequest) {
        Objects.requireNonNull(processRequest, "processRequest cannot be null");
        return httpClient.process(processRequest);
    }

    /**
     * Submits a process request using a builder consumer.
     *
     * @param consumer configures the process request
     * @return the processing ID
     * @throws CICSdkException if the request fails
     */
    public String process(Consumer<ProcessRequest.Builder> consumer) {
        var builder = ProcessRequest.builder();
        consumer.accept(builder);
        return httpClient.process(builder.build());
    }

    /**
     * Gets enrichment results for a processing job.
     *
     * @param processingId the processing identifier
     * @return the enrichment result
     * @throws CICSdkException if the request fails
     */
    public EnrichmentResult getResults(String processingId) {
        Objects.requireNonNull(processingId, "processingId cannot be null");
        return httpClient.getResults(processingId);
    }

    /**
     * Uploads a blob and submits it for enrichment in one step. Returns the processing ID. The caller can then use
     * {@link #getResults(String)} or {@link #pollResults(String)} to get results.
     *
     * @param blob the blob to enrich
     * @param processRequest the process request with actions (objectKey will be overridden with the presigned URL key)
     * @return the processing ID
     * @throws CICSdkException if any step fails
     */
    public String sendForEnrichment(CICBlob blob, ProcessRequest processRequest) {
        Objects.requireNonNull(blob, "blob cannot be null");
        Objects.requireNonNull(processRequest, "processRequest cannot be null");

        var contentType = blob.getContentType().orElse("application/octet-stream");
        var presignedUrl = httpClient.getPresignedUrl(contentType);
        httpClient.upload(presignedUrl.presignedUrl(), blob);

        var actualRequest = ProcessRequest.builder()
                                          .objectKey(presignedUrl.objectKey())
                                          .actions(processRequest.actions())
                                          .build();

        return httpClient.process(actualRequest);
    }

    /**
     * Polls for enrichment results until they are ready or timeout.
     *
     * @param processingId the processing identifier
     * @return the enrichment result
     * @throws CICSdkException if polling times out or the request fails
     */
    public EnrichmentResult pollResults(String processingId) {
        Objects.requireNonNull(processingId, "processingId cannot be null");

        for (int attempt = 1; attempt <= pollMaxAttempts; attempt++) {
            if (attempt > 1) {
                sleep(pollIntervalMs);
            }

            var result = httpClient.getResultsIfReady(processingId);
            if (result != null) {
                return result;
            }
        }

        throw new CICSdkException("Enrichment polling timed out after " + pollMaxAttempts
                + " attempts for processing ID: " + processingId);
    }

    /**
     * End-to-end enrichment workflow: upload, process, poll until done, return results.
     *
     * @param blob the blob to enrich
     * @param actions the list of enrichment actions to perform (each with empty configuration)
     * @return the enrichment result
     * @throws CICSdkException if any step fails or polling times out
     */
    public EnrichmentResult enrich(CICBlob blob, List<Action> actions) {
        Objects.requireNonNull(blob, "blob cannot be null");
        Objects.requireNonNull(actions, "actions cannot be null");

        var builder = ProcessRequest.builder().objectKey("placeholder");
        actions.forEach(builder::action);
        var processingId = sendForEnrichment(blob, builder.build());
        return pollResults(processingId);
    }

    /**
     * End-to-end enrichment workflow with a process request builder consumer.
     *
     * @param blob the blob to enrich
     * @param consumer configures the process request (objectKey will be overridden)
     * @return the enrichment result
     * @throws CICSdkException if any step fails or polling times out
     */
    public EnrichmentResult enrich(CICBlob blob, Consumer<ProcessRequest.Builder> consumer) {
        Objects.requireNonNull(blob, "blob cannot be null");

        var builder = ProcessRequest.builder().objectKey("placeholder");
        consumer.accept(builder);
        var processingId = sendForEnrichment(blob, builder.build());
        return pollResults(processingId);
    }

    /**
     * Lists available enrichment actions.
     *
     * @return the actions response as a string
     * @throws CICSdkException if the request fails
     */
    public String getActions() {
        return httpClient.getActions();
    }

    /**
     * Lists available enrichment actions as typed descriptors, including any available models and categories.
     *
     * @return the list of action descriptors
     * @throws CICSdkException if the request fails
     * @since 1.0.0
     */
    public List<ActionDescriptor> getActionDescriptors() {
        return httpClient.getActionDescriptors();
    }

    /**
     * Checks if the Context Service is available.
     *
     * @return true if healthy
     */
    public boolean isHealthy() {
        return httpClient.isHealthy();
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CICSdkException("Interrupted while polling for enrichment results", e);
        }
    }
}
