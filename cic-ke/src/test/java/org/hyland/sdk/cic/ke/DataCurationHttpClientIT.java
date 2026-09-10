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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.NormalizationOptions;
import org.hyland.sdk.cic.ke.object.PiiOptions;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;

/**
 * End-to-end tests for {@link DataCurationHttpClient} against the staging CIC Data Curation API.
 * <p>
 * Requires the following environment variables:
 * <ul>
 * <li>{@code CIC_KE_DC_BASE_URL} - Data Curation API base URL</li>
 * <li>{@code CIC_KE_AUTH_URL} - OAuth2 token endpoint base URL</li>
 * <li>{@code CIC_KE_CLIENT_ID} - OAuth2 client ID</li>
 * <li>{@code CIC_KE_CLIENT_SECRET} - OAuth2 client secret</li>
 * </ul>
 * <p>
 * Sample files are loaded from {@code src/test/resources/e2e/}.
 *
 * @since 1.0.0
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataCurationHttpClientIT {

    private static final String SAMPLE_DOCUMENT = "/e2e/sample-document.txt";

    private static final String SAMPLE_PII = "/e2e/sample-pii.txt";

    private static DataCurationHttpClient client;

    private static DataCurationService service;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("CIC_KE_CLIENT_ID") != null, "Skipping E2E: CIC_KE_CLIENT_ID not set");
        assumeTrue(System.getenv("CIC_KE_CLIENT_SECRET") != null, "Skipping E2E: CIC_KE_CLIENT_SECRET not set");
        assumeTrue(System.getenv("CIC_KE_DC_BASE_URL") != null, "Skipping E2E: CIC_KE_DC_BASE_URL not set");
        assumeTrue(System.getenv("CIC_KE_AUTH_URL") != null, "Skipping E2E: CIC_KE_AUTH_URL not set");

        client = DataCurationHttpClient.from(System.getenv("CIC_KE_DC_BASE_URL"),
                AuthenticationHttpClient.from(System.getenv("CIC_KE_AUTH_URL"))
                                        .clientId(System.getenv("CIC_KE_CLIENT_ID"))
                                        .clientSecret(System.getenv("CIC_KE_CLIENT_SECRET")))
                                       .build();

        service = new DataCurationService(client);
        service.setPollSettings(30, Duration.ofSeconds(5));
    }

    // -------------------------------------------------------
    // Basic connectivity tests
    // -------------------------------------------------------

    @Test
    @Order(1)
    void healthCheckReturnsTrue() {
        assertTrue(client.isHealthy());
    }

    @Test
    @Order(2)
    void listModelsReturnsNonEmptyList() {
        var models = client.listModels();

        assertNotNull(models);
        assertFalse(models.isEmpty());
        assertNotNull(models.get(0).name());
    }

    // -------------------------------------------------------
    // Pipeline workflow tests
    // -------------------------------------------------------

    @Test
    @Order(10)
    void fullCurationWorkflow() throws InterruptedException {
        var options = ProcessingOptions.builder().chunking(true).chunkSize(1000).build();

        var presignResponse = client.presign(options);
        assertNotNull(presignResponse.jobId());
        assertNotNull(presignResponse.putUrl());
        assertNotNull(presignResponse.getUrl());

        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "application/octet-stream");
        client.upload(presignResponse.putUrl(), blob);

        JobStatus status = pollForCompletion(presignResponse.jobId(), 30, 5000);
        assertNotNull(status, "Job did not reach terminal state within timeout");
        assertTrue(status.isTerminal(), "Expected terminal status but got: " + status.status());
        assertTrue(status.isDone(), "Job failed with error: " + status.errorMessage());

        var result = client.downloadResult(presignResponse.getUrl());
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @Order(11)
    void curationWithEmbeddings() throws InterruptedException {
        var options = ProcessingOptions.builder().chunking(true).chunkSize(1000).embedding(true).build();

        var presignResponse = client.presign(options);
        assertNotNull(presignResponse.jobId());

        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "application/octet-stream");
        client.upload(presignResponse.putUrl(), blob);

        JobStatus status = pollForCompletion(presignResponse.jobId(), 30, 5000);
        assertNotNull(status, "Job did not reach terminal state within timeout");
        assertTrue(status.isDone(), "Job failed with error: " + status.errorMessage());

        var result = client.downloadResult(presignResponse.getUrl());
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @Order(12)
    void curationWithNormalization() throws InterruptedException {
        var options = ProcessingOptions.builder()
                                       .chunking(true)
                                       .chunkSize(1000)
                                       .normalization(
                                               NormalizationOptions.builder().quotations(true).dashes(true).build())
                                       .build();

        var presignResponse = client.presign(options);
        assertNotNull(presignResponse.jobId());

        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "application/octet-stream");
        client.upload(presignResponse.putUrl(), blob);

        JobStatus status = pollForCompletion(presignResponse.jobId(), 30, 5000);
        assertNotNull(status, "Job did not reach terminal state within timeout");
        assertTrue(status.isDone(), "Job failed with error: " + status.errorMessage());

        var result = client.downloadResult(presignResponse.getUrl());
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    @Order(13)
    void curationWithPiiRedaction() throws InterruptedException {
        var options = ProcessingOptions.builder()
                                       .chunking(true)
                                       .chunkSize(1000)
                                       .pii(PiiOptions.builder().mode("redaction").build())
                                       .build();

        var presignResponse = client.presign(options);
        assertNotNull(presignResponse.jobId());

        var blob = loadResourceBlob(SAMPLE_PII, "application/octet-stream");
        client.upload(presignResponse.putUrl(), blob);

        JobStatus status = pollForCompletion(presignResponse.jobId(), 30, 5000);
        assertNotNull(status, "Job did not reach terminal state within timeout");
        assertTrue(status.isDone(), "PII redaction job failed with error: " + status.errorMessage());

        var result = client.downloadResult(presignResponse.getUrl());
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    // -------------------------------------------------------
    // Config defaults tests
    // -------------------------------------------------------

    @Test
    @Order(20)
    void configDefaultsReadAndReset() {
        try {
            var defaults = client.getConfigDefaults();
            assertNotNull(defaults);
        } catch (CICServiceException e) {
            assumeTrue(e.statusCode() != 403, "Skipping: service account lacks configuration permissions (HTTP 403)");
            throw e;
        }

        client.resetConfigDefaults();

        var afterReset = client.getConfigDefaults();
        assertNotNull(afterReset);
    }

    // -------------------------------------------------------
    // Config rules lifecycle tests
    // -------------------------------------------------------

    @Test
    @Order(30)
    void configRulesCrudLifecycle() {
        List<ConfigRule> initialRules;
        try {
            initialRules = client.listConfigRules();
        } catch (CICServiceException e) {
            assumeTrue(e.statusCode() != 403, "Skipping: service account lacks configuration permissions (HTTP 403)");
            throw e;
        }
        assertNotNull(initialRules);

        var ruleToCreate = ConfigRule.builder()
                                     .name("e2e-test-rule")
                                     .addCondition("content_type", "application/pdf")
                                     .config(ProcessingOptions.builder().chunking(true).chunkSize(2000).build())
                                     .build();

        ConfigRule created = client.createConfigRule(ruleToCreate);
        assertNotNull(created);
        assertNotNull(created.id());
        assertTrue(created.name().contains("e2e-test-rule"));

        try {
            ConfigRule fetched = client.getConfigRule(created.id());
            assertNotNull(fetched);
            assertNotNull(fetched.id());
            assertTrue(fetched.name().contains("e2e-test-rule"));
        } finally {
            client.deleteConfigRule(created.id());
        }

        List<ConfigRule> afterDelete = client.listConfigRules();
        boolean ruleStillExists = afterDelete.stream().anyMatch(r -> created.id().equals(r.id()));
        assertFalse(ruleStillExists, "Rule should have been deleted");
    }

    // -------------------------------------------------------
    // DataCurationService high-level workflow test
    // -------------------------------------------------------

    @Test
    @Order(40)
    void dataCurationServiceCurateWorkflow() {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "application/octet-stream");
        var options = ProcessingOptions.builder().chunking(true).chunkSize(1000).build();

        String result = service.curate(blob, options);

        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private JobStatus pollForCompletion(String jobId, int maxAttempts, long intervalMs) throws InterruptedException {
        for (int i = 0; i < maxAttempts; i++) {
            var status = client.getJobStatus(jobId);
            if (status.isTerminal()) {
                return status;
            }
            Thread.sleep(intervalMs);
        }
        return null;
    }

    private static CICBlob loadResourceBlob(String resourcePath, String contentType) {
        return new CICBlob() {

            @Override
            public InputStream getInputStream() {
                InputStream is = DataCurationHttpClientIT.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new IllegalStateException("Test resource not found: " + resourcePath);
                }
                return is;
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getContentType() {
                return Optional.of(contentType);
            }
        };
    }
}
