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
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;

/**
 * High-level service for the Data Curation API providing end-to-end workflows.
 *
 * @since 1.0.0
 */
public class DataCurationService {

    protected final DataCurationHttpClient httpClient;

    private int pollMaxAttempts = 20;

    private long pollIntervalMs = 5000;

    public DataCurationService(DataCurationHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Configures the polling behavior for the {@link #curate} method.i
     *
     * @param maxAttempts the maximum number of polling attempts
     * @param intervalMs the sleep interval between polls in milliseconds
     */
    public void setPollSettings(int maxAttempts, long intervalMs) {
        this.pollMaxAttempts = maxAttempts;
        this.pollIntervalMs = intervalMs;
    }

    // ---------------
    // Pipeline APIs
    // ---------------

    /**
     * Generates presigned URLs for file upload and result retrieval.
     *
     * @param options the processing options (may be null for defaults)
     * @return the presign response
     * @throws CICSdkException if the request fails
     */
    public PresignResponse presign(ProcessingOptions options) {
        return httpClient.presign(options);
    }

    /**
     * Generates presigned URLs using a builder consumer for processing options.
     *
     * @param consumer configures the processing options
     * @return the presign response
     * @throws CICSdkException if the request fails
     */
    public PresignResponse presign(Consumer<ProcessingOptions.Builder> consumer) {
        var builder = ProcessingOptions.builder();
        consumer.accept(builder);
        return httpClient.presign(builder.build());
    }

    /**
     * Gets the status of a curation job.
     *
     * @param jobId the job identifier
     * @return the job status
     * @throws CICSdkException if the request fails
     */
    public JobStatus getJobStatus(String jobId) {
        Objects.requireNonNull(jobId, "jobId cannot be null");
        return httpClient.getJobStatus(jobId);
    }

    /**
     * End-to-end data curation workflow: presign, upload, poll until done, download results.
     *
     * @param blob the blob to curate
     * @param options the processing options (may be null for defaults)
     * @return the curation result as a JSON string
     * @throws CICSdkException if any step fails or polling times out
     */
    public String curate(CICBlob blob, ProcessingOptions options) {
        Objects.requireNonNull(blob, "blob cannot be null");

        // 1. Get presigned URLs
        var presignResponse = httpClient.presign(options);

        // 2. Upload
        httpClient.upload(presignResponse.putUrl(), blob);

        // 3. Poll status
        for (int attempt = 1; attempt <= pollMaxAttempts; attempt++) {
            if (attempt > 1) {
                sleep(pollIntervalMs);
            }

            var status = httpClient.getJobStatus(presignResponse.jobId());
            if (status.isDone()) {
                // 4. Download result
                return httpClient.downloadResult(presignResponse.getUrl());
            }
        }

        throw new CICSdkException("Data curation polling timed out after " + pollMaxAttempts + " attempts for job: "
                + presignResponse.jobId());
    }

    /**
     * End-to-end data curation using a builder consumer for processing options.
     *
     * @param blob the blob to curate
     * @param consumer configures the processing options
     * @return the curation result as a JSON string
     * @throws CICSdkException if any step fails or polling times out
     */
    public String curate(CICBlob blob, Consumer<ProcessingOptions.Builder> consumer) {
        var builder = ProcessingOptions.builder();
        consumer.accept(builder);
        return curate(blob, builder.build());
    }

    /**
     * Lists available embedding models.
     *
     * @return the list of embedding models
     * @throws CICSdkException if the request fails
     */
    public List<EmbeddingModel> listModels() {
        return httpClient.listModels();
    }

    // ---------------
    // Config APIs
    // ---------------

    /**
     * Initializes configuration with system defaults.
     *
     * @return the created configuration
     * @throws CICSdkException if the request fails
     */
    public ConfigOptions initializeConfig() {
        return httpClient.initializeConfig();
    }

    /**
     * Retrieves complete configuration (defaults + rules).
     *
     * @return the full configuration
     * @throws CICSdkException if the request fails
     */
    public ConfigOptions getConfig() {
        return httpClient.getConfig();
    }

    /**
     * Retrieves only the defaults section.
     *
     * @return the processing options defaults
     * @throws CICSdkException if the request fails
     */
    public ProcessingOptions getConfigDefaults() {
        return httpClient.getConfigDefaults();
    }

    /**
     * Replaces environment defaults.
     *
     * @param defaults the new defaults
     * @return the updated defaults
     * @throws CICSdkException if the request fails
     */
    public ProcessingOptions updateConfigDefaults(ProcessingOptions defaults) {
        Objects.requireNonNull(defaults, "defaults cannot be null");
        return httpClient.updateConfigDefaults(defaults);
    }

    /**
     * Resets defaults to system defaults.
     *
     * @throws CICSdkException if the request fails
     */
    public void resetConfigDefaults() {
        httpClient.resetConfigDefaults();
    }

    /**
     * Retrieves all rules.
     *
     * @return the list of rules
     * @throws CICSdkException if the request fails
     */
    public List<ConfigRule> listConfigRules() {
        return httpClient.listConfigRules();
    }

    /**
     * Adds a new conditional rule.
     *
     * @param rule the rule to create
     * @return the created rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule createConfigRule(ConfigRule rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return httpClient.createConfigRule(rule);
    }

    /**
     * Adds a new conditional rule using a builder consumer.
     *
     * @param consumer configures the rule
     * @return the created rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule createConfigRule(Consumer<ConfigRule.Builder> consumer) {
        var builder = ConfigRule.builder();
        consumer.accept(builder);
        return httpClient.createConfigRule(builder.build());
    }

    /**
     * Retrieves a specific rule.
     *
     * @param ruleId the rule identifier
     * @return the rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule getConfigRule(String ruleId) {
        Objects.requireNonNull(ruleId, "ruleId cannot be null");
        return httpClient.getConfigRule(ruleId);
    }

    /**
     * Modifies an existing rule.
     *
     * @param ruleId the rule identifier
     * @param rule the updated rule
     * @return the updated rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule updateConfigRule(String ruleId, ConfigRule rule) {
        Objects.requireNonNull(ruleId, "ruleId cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return httpClient.updateConfigRule(ruleId, rule);
    }

    /**
     * Removes a rule.
     *
     * @param ruleId the rule identifier
     * @throws CICSdkException if the request fails
     */
    public void deleteConfigRule(String ruleId) {
        Objects.requireNonNull(ruleId, "ruleId cannot be null");
        httpClient.deleteConfigRule(ruleId);
    }

    /**
     * Preview rule matching without applying changes (dry run).
     *
     * @param testRequest the test request with document properties
     * @return the test response
     * @throws CICSdkException if the request fails
     */
    public RuleTestResponse testConfigRules(RuleTestRequest testRequest) {
        Objects.requireNonNull(testRequest, "testRequest cannot be null");
        return httpClient.testConfigRules(testRequest);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CICSdkException("Interrupted while polling for curation results", e);
        }
    }
}
