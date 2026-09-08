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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ActionDescriptor;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.ObjectKey;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * High-level service for the Context (Knowledge Enrichment) API providing end-to-end workflows.
 *
 * @since 1.1.0
 */
public class KEService {

    protected final KEHttpClient httpClient;

    private int pollMaxAttempts = 20;

    private Duration pollInterval = Duration.ofSeconds(5);

    public KEService(KEHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Configures the polling behavior for the {@link #enrich} methods.
     * <p>
     * <b>Thread safety:</b> This method mutates shared state. If multiple threads share this service instance, callers
     * must synchronize externally or configure poll settings before sharing the instance.
     *
     * @param maxAttempts the maximum number of polling attempts (must be at least 1)
     * @param interval the sleep interval between polls (must be non-negative)
     * @throws IllegalArgumentException if maxAttempts is less than 1 or interval is negative
     */
    public void setPollSettings(int maxAttempts, Duration interval) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1, got: " + maxAttempts);
        }
        Objects.requireNonNull(interval, "interval cannot be null");
        if (interval.isNegative()) {
            throw new IllegalArgumentException("interval must be non-negative");
        }
        this.pollMaxAttempts = maxAttempts;
        this.pollInterval = interval;
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
     * Configures a process request, uploads the blob, and submits it for enrichment in one step. The uploaded object's
     * key is added to the request after upload, so callers only need to configure actions.
     *
     * @param blob the blob to enrich
     * @param consumer configures the process request actions
     * @return the processing ID
     * @throws CICSdkException if any step fails
     */
    public String sendForEnrichment(CICBlob blob, Consumer<ProcessRequest.Builder> consumer) {
        Objects.requireNonNull(blob, "blob cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");

        var builder = ProcessRequest.builder();
        consumer.accept(builder);

        var contentType = blob.getContentType().orElse("application/octet-stream");
        var presignedUrl = httpClient.getPresignedUrl(contentType);

        builder.objectKeys(List.of(ObjectKey.forPath(presignedUrl.objectKey())));
        var processRequest = builder.build();

        httpClient.upload(presignedUrl.presignedUrl(), blob);
        return httpClient.process(processRequest);
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
                sleep(pollInterval.toMillis());
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

        var processingId = sendForEnrichment(blob, builder -> actions.forEach(builder::action));
        return pollResults(processingId);
    }

    /**
     * End-to-end enrichment workflow with a process request builder consumer.
     *
     * @param blob the blob to enrich
     * @param consumer configures the process request actions; the uploaded object's key is added automatically
     * @return the enrichment result
     * @throws CICSdkException if any step fails or polling times out
     */
    public EnrichmentResult enrich(CICBlob blob, Consumer<ProcessRequest.Builder> consumer) {
        Objects.requireNonNull(blob, "blob cannot be null");
        Objects.requireNonNull(consumer, "consumer cannot be null");

        var processingId = sendForEnrichment(blob, consumer);
        return pollResults(processingId);
    }

    /**
     * Lists available enrichment actions as typed descriptors, including any available models and categories.
     *
     * @return the list of action descriptors
     * @throws CICSdkException if the request fails
     * @since 1.1.0
     */
    public List<ActionDescriptor> getActionDescriptors() {
        return httpClient.getActionDescriptors();
    }

    /**
     * Returns version usage statistics for content processing.
     *
     * @return a map of version identifiers to their usage counts
     * @throws CICSdkException if the request fails
     * @since 1.1.0
     */
    public Map<String, Long> getVersionUsageStats() {
        return httpClient.getVersionUsageStats();
    }

    /**
     * Checks availability of the Context Service. Returns {@code true} only when the service responds with HTTP 200.
     * Authentication failures, network errors, and all other status codes return {@code false}.
     *
     * @return true if the service is healthy and reachable
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
