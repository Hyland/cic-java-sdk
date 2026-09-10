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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.DELETE;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.PUT;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.HealthDetails;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;

/**
 * HTTP client for interacting with the CIC Data Curation API.
 *
 * @since 1.1.0
 */
public class DataCurationHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String PRESIGN_PATH = "/presign";

    private static final String STATUS_PATH = "/status";

    private static final String MODELS_PATH = "/models";

    private static final String HEALTH_PATH = "/health";

    private static final String HEALTH_DETAILS_PATH = "/health/details";

    private static final String CONFIG_OPTIONS_PATH = "/config/options";

    private static final String CONFIG_DEFAULTS_PATH = "/config/options/defaults";

    private static final String CONFIG_RULES_PATH = "/config/options/rules";

    private static final String CONFIG_RULES_TEST_PATH = "/config/options/rules/test";

    // ---------------
    // Instantiation
    // ---------------

    protected DataCurationHttpClient(Builder builder) {
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
     * Generates presigned S3 URLs to upload a file and retrieve job results.
     *
     * @param options the processing options for the pipeline
     * @return the presign response containing job_id, put_url, and get_url
     * @throws CICSdkException if the request fails
     */
    public PresignResponse presign(ProcessingOptions options) {
        var requestBuilder = this.requestBuilder(POST, PRESIGN_PATH).header("Content-Type", "application/json");
        if (options != null) {
            requestBuilder.entity(new CICEntity(options));
        }
        return sendThenMapAs(requestBuilder.build(), PresignResponse.class);
    }

    /**
     * Retrieves the processing status for a submitted job.
     *
     * @param jobId the job identifier
     * @return the job status
     * @throws CICSdkException if the request fails
     */
    public JobStatus getJobStatus(String jobId) {
        var request = this.requestBuilder(GET, STATUS_PATH + "/" + encodePathSegment(jobId)).build();
        return sendThenMapAs(request, JobStatus.class);
    }

    /**
     * Lists available embedding models and their configurations.
     *
     * @return a list of embedding models
     * @throws CICSdkException if the request fails
     */
    public List<EmbeddingModel> listModels() {
        var request = this.requestBuilder(GET, MODELS_PATH).build();
        return sendThenMapAs(request, EmbeddingModel.ListOf.class);
    }

    /**
     * Uploads a blob to the given presigned PUT URL with retry support.
     *
     * @param putUrl the presigned PUT URL
     * @param blob the blob to upload
     * @throws CICSdkException if upload fails
     */
    public void upload(String putUrl, CICBlob blob) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("cic-dc-upload-", ".tmp");
            try (var is = blob.getInputStream()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            Path finalTempFile = tempFile;
            var response = sendRawWithRetry(() -> {
                try {
                    var uploadRequest = HttpRequest.newBuilder(URI.create(putUrl))
                                                   .PUT(HttpRequest.BodyPublishers.ofFile(finalTempFile))
                                                   .header("Content-Type", "application/octet-stream");
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
     * Downloads the curation result from a presigned GET URL.
     *
     * @param getUrl the presigned GET URL
     * @return the result body as a string
     * @throws CICSdkException if download fails
     */
    public String downloadResult(String getUrl) {
        var response = sendRawWithRetry(() -> {
            var downloadRequest = HttpRequest.newBuilder(URI.create(getUrl)).GET();
            if (requestTimeout != null) {
                downloadRequest.timeout(requestTimeout);
            }
            return downloadRequest.build();
        }, "GET", HttpResponse.BodyHandlers.ofString(), statusCode -> statusCode == 200);
        if (response.statusCode() != 200) {
            throw new CICSdkException(
                    "Couldn't download results, HTTP response returned with status code: " + response.statusCode());
        }
        return response.body();
    }

    // ---------------
    // Health APIs
    // ---------------

    /**
     * Checks availability of the Data Curation service. Returns {@code true} only when the service responds with HTTP
     * 200. Authentication failures, network errors, and all other status codes return {@code false}.
     * <p>
     * Note: This endpoint works without authentication, but the SDK currently sends credentials because the client
     * inherits authenticated request handling. Use {@link #getHealthDetails()} for richer diagnostics.
     *
     * @return true if the service is healthy and reachable
     */
    public boolean isHealthy() {
        var response = sendThenReadAsString(this.requestBuilder(GET, HEALTH_PATH).build());
        return response.statusCode() == 200;
    }

    /**
     * Returns detailed health information including application version, system metrics, and dependency checks.
     *
     * @return the detailed health information
     * @throws CICSdkException if the request fails
     * @since 1.1.0
     */
    public HealthDetails getHealthDetails() {
        var request = this.requestBuilder(GET, HEALTH_DETAILS_PATH).build();
        return sendThenMapAs(request, HealthDetails.class);
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
        var request = this.requestBuilder(POST, CONFIG_OPTIONS_PATH).build();
        return sendThenMapAs(request, ConfigOptions.class);
    }

    /**
     * Retrieves complete configuration (defaults + rules).
     *
     * @return the full configuration
     * @throws CICSdkException if the request fails
     */
    public ConfigOptions getConfig() {
        var request = this.requestBuilder(GET, CONFIG_OPTIONS_PATH).build();
        return sendThenMapAs(request, ConfigOptions.class);
    }

    /**
     * Retrieves only the defaults section.
     *
     * @return the processing options defaults
     * @throws CICSdkException if the request fails
     */
    public ProcessingOptions getConfigDefaults() {
        var request = this.requestBuilder(GET, CONFIG_DEFAULTS_PATH).build();
        return sendThenMapAs(request, ProcessingOptions.class);
    }

    /**
     * Replaces environment defaults.
     *
     * @param defaults the new defaults
     * @return the updated defaults
     * @throws CICSdkException if the request fails
     */
    public ProcessingOptions updateConfigDefaults(ProcessingOptions defaults) {
        var request = this.requestBuilder(PUT, CONFIG_DEFAULTS_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(defaults))
                          .build();
        return sendThenMapAs(request, ProcessingOptions.class);
    }

    /**
     * Resets defaults to system defaults.
     *
     * @throws CICSdkException if the request fails
     */
    public void resetConfigDefaults() {
        var response = sendThenReadAsString(this.requestBuilder(DELETE, CONFIG_DEFAULTS_PATH).build());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to reset config defaults, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    /**
     * Retrieves all rules.
     *
     * @return the list of rules
     * @throws CICSdkException if the request fails
     */
    public List<ConfigRule> listConfigRules() {
        var request = this.requestBuilder(GET, CONFIG_RULES_PATH).build();
        return sendThenMapAs(request, ConfigRule.ListOf.class);
    }

    /**
     * Adds a new conditional rule.
     *
     * @param rule the rule to create
     * @return the created rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule createConfigRule(ConfigRule rule) {
        var request = this.requestBuilder(POST, CONFIG_RULES_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(rule))
                          .build();
        return sendThenMapAs(request, ConfigRule.class);
    }

    /**
     * Retrieves a specific rule.
     *
     * @param ruleId the rule identifier
     * @return the rule
     * @throws CICSdkException if the request fails
     */
    public ConfigRule getConfigRule(String ruleId) {
        var request = this.requestBuilder(GET, CONFIG_RULES_PATH + "/" + encodePathSegment(ruleId)).build();
        return sendThenMapAs(request, ConfigRule.class);
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
        var request = this.requestBuilder(PUT, CONFIG_RULES_PATH + "/" + encodePathSegment(ruleId))
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(rule))
                          .build();
        return sendThenMapAs(request, ConfigRule.class);
    }

    /**
     * Removes a rule.
     *
     * @param ruleId the rule identifier
     * @throws CICSdkException if the request fails
     */
    public void deleteConfigRule(String ruleId) {
        var response = sendThenReadAsString(
                this.requestBuilder(DELETE, CONFIG_RULES_PATH + "/" + encodePathSegment(ruleId)).build());
        if (response.statusCode() != 200 && response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete config rule, HTTP response returned with status code: " + response.statusCode());
        }
    }

    /**
     * Preview rule matching without applying changes (dry run).
     *
     * @param testRequest the test request with document properties
     * @return the test response showing matched rule and effective config
     * @throws CICSdkException if the request fails
     */
    public RuleTestResponse testConfigRules(RuleTestRequest testRequest) {
        var request = this.requestBuilder(POST, CONFIG_RULES_TEST_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(testRequest))
                          .build();
        return sendThenMapAs(request, RuleTestResponse.class);
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, DataCurationHttpClient> {

        /**
         * Creates a builder for DataCurationHttpClient.
         *
         * @param baseUrl the base URL of the Data Curation API
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
        public DataCurationHttpClient build() {
            return new DataCurationHttpClient(this);
        }
    }
}
