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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.HealthDetails;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.Percentage;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;

/**
 * @since 1.0.0
 */
class DataCurationServiceTest {

    private TestDataCurationHttpClient httpClient;

    private DataCurationService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestDataCurationHttpClient();
        service = new DataCurationService(httpClient);
        service.setPollSettings(3, Duration.ofMillis(10));
    }

    @Test
    void testPresign() {
        httpClient.presignResponse = new PresignResponse("job-1", "https://put.url", "https://get.url", null);

        var response = service.presign((ProcessingOptions) null);

        assertNotNull(response);
        assertEquals("job-1", response.jobId());
        assertEquals(1, httpClient.presignCalls);
    }

    @Test
    void testGetJobStatus() {
        httpClient.jobStatus = new JobStatus("job-1", "Done");

        var status = service.getJobStatus("job-1");

        assertNotNull(status);
        assertEquals("Done", status.status());
    }

    @Test
    void testCurateEndToEnd() {
        httpClient.presignResponse = new PresignResponse("job-1", "https://put.url", "https://get.url", null);
        httpClient.jobStatus = new JobStatus("job-1", "Done");
        httpClient.downloadResultBody = "{\"markdown\":{\"output\":\"Test\"}}";

        CICBlob blob = createTestBlob();
        var result = service.curate(blob, (ProcessingOptions) null);

        assertNotNull(result);
        assertEquals("{\"markdown\":{\"output\":\"Test\"}}", result);
        assertEquals(1, httpClient.presignCalls);
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals(1, httpClient.getJobStatusCalls);
        assertEquals(1, httpClient.downloadResultCalls);
    }

    @Test
    void testCurateWithPolling() {
        httpClient.presignResponse = new PresignResponse("job-1", "https://put.url", "https://get.url", null);
        httpClient.jobStatuses.add(new JobStatus("job-1", "Wait For Upload"));
        httpClient.jobStatuses.add(new JobStatus("job-1", "Processing"));
        httpClient.jobStatuses.add(new JobStatus("job-1", "Done"));
        httpClient.downloadResultBody = "{\"result\":\"ok\"}";

        CICBlob blob = createTestBlob();
        var result = service.curate(blob, (ProcessingOptions) null);

        assertNotNull(result);
        assertEquals(3, httpClient.getJobStatusCalls);
    }

    @Test
    void testCurateTimeout() {
        httpClient.presignResponse = new PresignResponse("job-1", "https://put.url", "https://get.url", null);
        httpClient.jobStatus = new JobStatus("job-1", "Processing");

        CICBlob blob = createTestBlob();
        assertThrows(CICSdkException.class, () -> service.curate(blob, (ProcessingOptions) null));
    }

    @Test
    void testCurateFailedJobThrowsImmediately() {
        httpClient.presignResponse = new PresignResponse("job-f", "https://put.url", "https://get.url", null);
        httpClient.jobStatuses.add(new JobStatus("job-f", "Processing"));
        httpClient.jobStatuses.add(new JobStatus("job-f", "FAILED", "pipeline exhausted retries"));

        CICBlob blob = createTestBlob();
        var ex = assertThrows(CICSdkException.class, () -> service.curate(blob, (ProcessingOptions) null));

        assertTrue(ex.getMessage().contains("failed with status"));
        assertTrue(ex.getMessage().contains("pipeline exhausted retries"));
        assertEquals(2, httpClient.getJobStatusCalls);
        assertEquals(0, httpClient.downloadResultCalls);
    }

    @Test
    void testCurateFailedJobWithoutErrorMessage() {
        httpClient.presignResponse = new PresignResponse("job-f2", "https://put.url", "https://get.url", null);
        httpClient.jobStatus = new JobStatus("job-f2", "FAILED");

        CICBlob blob = createTestBlob();
        var ex = assertThrows(CICSdkException.class, () -> service.curate(blob, (ProcessingOptions) null));

        assertTrue(ex.getMessage().contains("failed with status"));
        assertFalse(ex.getMessage().contains("—"));
        assertEquals(0, httpClient.downloadResultCalls);
    }

    @Test
    void testCurateCompletedWithErrorThrows() {
        httpClient.presignResponse = new PresignResponse("job-ce", "https://put.url", "https://get.url", null);
        httpClient.jobStatus = new JobStatus("job-ce", "COMPLETED", "delivery failed");

        CICBlob blob = createTestBlob();
        var ex = assertThrows(CICSdkException.class, () -> service.curate(blob, (ProcessingOptions) null));

        assertTrue(ex.getMessage().contains("completed with error"));
        assertTrue(ex.getMessage().contains("delivery failed"));
        assertEquals(0, httpClient.downloadResultCalls);
    }

    @Test
    void testListModels() {
        httpClient.models = List.of(
                new EmbeddingModel("model-one", 512, List.of("float32"), List.of(1024), List.of("search_document")));

        var models = service.listModels();

        assertEquals(1, models.size());
        assertEquals("model-one", models.get(0).name());
    }

    // --- Consumer overloads ---

    @Test
    void testPresignWithConsumer() {
        httpClient.presignResponse = new PresignResponse("job-c", "https://put.url", "https://get.url", null);

        var response = service.presign(opts -> opts.chunking(true).chunkSize(1000));

        assertNotNull(response);
        assertEquals("job-c", response.jobId());
        assertEquals(1, httpClient.presignCalls);
        assertNotNull(httpClient.lastPresignOptions);
        assertEquals(true, httpClient.lastPresignOptions.chunking());
        assertEquals(1000, httpClient.lastPresignOptions.chunkSize());
    }

    @Test
    void testCurateWithConsumer() {
        httpClient.presignResponse = new PresignResponse("job-cc", "https://put.url", "https://get.url", null);
        httpClient.jobStatus = new JobStatus("job-cc", "Done");
        httpClient.downloadResultBody = "{\"result\":\"consumer\"}";

        CICBlob blob = createTestBlob();
        var result = service.curate(blob, opts -> opts.embedding(true));

        assertNotNull(result);
        assertEquals("{\"result\":\"consumer\"}", result);
        assertNotNull(httpClient.lastPresignOptions);
        assertEquals(true, httpClient.lastPresignOptions.embedding());
    }

    // --- Config pass-through methods ---

    @Test
    void testInitializeConfig() {
        httpClient.configOptions = new ConfigOptions(ProcessingOptions.builder().chunking(true).build(), List.of());

        var config = service.initializeConfig();

        assertNotNull(config);
        assertEquals(true, config.defaults().chunking());
        assertEquals(1, httpClient.initializeConfigCalls);
    }

    @Test
    void testGetConfig() {
        httpClient.configOptions = new ConfigOptions(ProcessingOptions.builder().build(), List.of());

        var config = service.getConfig();

        assertNotNull(config);
        assertEquals(1, httpClient.getConfigCalls);
    }

    @Test
    void testGetConfigDefaults() {
        httpClient.configDefaults = ProcessingOptions.builder().embedding(true).build();

        var defaults = service.getConfigDefaults();

        assertNotNull(defaults);
        assertEquals(true, defaults.embedding());
        assertEquals(1, httpClient.getConfigDefaultsCalls);
    }

    @Test
    void testUpdateConfigDefaults() {
        var newDefaults = ProcessingOptions.builder().chunking(false).build();
        httpClient.configDefaults = newDefaults;

        var result = service.updateConfigDefaults(newDefaults);

        assertNotNull(result);
        assertEquals(1, httpClient.updateConfigDefaultsCalls);
    }

    @Test
    void testUpdateConfigDefaultsNullThrows() {
        assertThrows(NullPointerException.class, () -> service.updateConfigDefaults(null));
    }

    @Test
    void testResetConfigDefaults() {
        service.resetConfigDefaults();

        assertEquals(1, httpClient.resetConfigDefaultsCalls);
    }

    @Test
    void testListConfigRules() {
        var rule = ConfigRule.builder().id("r1").name("Rule One").build();
        httpClient.configRules = List.of(rule);

        var rules = service.listConfigRules();

        assertEquals(1, rules.size());
        assertEquals("Rule One", rules.get(0).name());
    }

    @Test
    void testCreateConfigRule() {
        var rule = ConfigRule.builder().name("New Rule").addCondition("type", "pdf").build();
        httpClient.lastConfigRule = rule;

        var created = service.createConfigRule(rule);

        assertNotNull(created);
        assertEquals("New Rule", created.name());
        assertEquals(1, httpClient.createConfigRuleCalls);
    }

    @Test
    void testCreateConfigRuleNullThrows() {
        assertThrows(NullPointerException.class, () -> service.createConfigRule((ConfigRule) null));
    }

    @Test
    void testCreateConfigRuleWithConsumer() {
        httpClient.lastConfigRule = ConfigRule.builder().name("Consumer Rule").build();

        var created = service.createConfigRule(r -> r.name("Consumer Rule").addCondition("size", "large"));

        assertNotNull(created);
        assertEquals(1, httpClient.createConfigRuleCalls);
    }

    @Test
    void testGetConfigRule() {
        httpClient.lastConfigRule = ConfigRule.builder().id("r1").name("Fetched").build();

        var rule = service.getConfigRule("r1");

        assertNotNull(rule);
        assertEquals("Fetched", rule.name());
        assertEquals("r1", httpClient.lastRuleId);
    }

    @Test
    void testGetConfigRuleNullThrows() {
        assertThrows(NullPointerException.class, () -> service.getConfigRule(null));
    }

    @Test
    void testUpdateConfigRule() {
        var updated = ConfigRule.builder().name("Updated").build();
        httpClient.lastConfigRule = updated;

        var result = service.updateConfigRule("r1", updated);

        assertNotNull(result);
        assertEquals("r1", httpClient.lastRuleId);
        assertEquals(1, httpClient.updateConfigRuleCalls);
    }

    @Test
    void testUpdateConfigRuleNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.updateConfigRule(null, ConfigRule.builder().build()));
    }

    @Test
    void testUpdateConfigRuleNullRuleThrows() {
        assertThrows(NullPointerException.class, () -> service.updateConfigRule("r1", null));
    }

    @Test
    void testDeleteConfigRule() {
        service.deleteConfigRule("r1");

        assertEquals("r1", httpClient.lastRuleId);
        assertEquals(1, httpClient.deleteConfigRuleCalls);
    }

    @Test
    void testDeleteConfigRuleNullThrows() {
        assertThrows(NullPointerException.class, () -> service.deleteConfigRule(null));
    }

    @Test
    void testTestConfigRules() {
        var testRequest = RuleTestRequest.builder().property("content_type", "application/pdf").build();
        httpClient.ruleTestResponse = new RuleTestResponse(ConfigRule.builder().name("Matched").build(),
                ProcessingOptions.builder().chunking(true).build());

        var response = service.testConfigRules(testRequest);

        assertNotNull(response);
        assertEquals("Matched", response.matchedRule().name());
        assertEquals(1, httpClient.testConfigRulesCalls);
    }

    @Test
    void testTestConfigRulesNullThrows() {
        assertThrows(NullPointerException.class, () -> service.testConfigRules(null));
    }

    // --- Health details ---

    @Test
    void testGetHealthDetails() {
        httpClient.healthDetails = new HealthDetails("healthy", "2026-08-26T12:54:31Z", "1.193.0-release",
                Duration.ofMillis(114396200L), new Percentage(0.0), new Percentage(19.3), new Percentage(21.0), true);

        var details = service.getHealthDetails();

        assertNotNull(details);
        assertEquals("healthy", details.status());
        assertEquals("1.193.0-release", details.applicationVersion());
        assertEquals(1, httpClient.getHealthDetailsCalls);
    }

    // --- Null validation for core methods ---

    @Test
    void testGetJobStatusNullThrows() {
        assertThrows(NullPointerException.class, () -> service.getJobStatus(null));
    }

    @Test
    void testSetPollSettingsRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(0, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(-1, Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(5, Duration.ofMillis(-1)));
    }

    @Test
    void testCurateNullBlobThrows() {
        assertThrows(NullPointerException.class, () -> service.curate(null, (ProcessingOptions) null));
    }

    private CICBlob createTestBlob() {
        return new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("test content".getBytes());
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getContentType() {
                return Optional.of("application/pdf");
            }
        };
    }

    private record UploadCall(String url, CICBlob blob) {
    }

    private static class TestDataCurationHttpClient extends DataCurationHttpClient {

        PresignResponse presignResponse;

        int presignCalls;

        ProcessingOptions lastPresignOptions;

        List<UploadCall> uploadCalls = new ArrayList<>();

        JobStatus jobStatus;

        List<JobStatus> jobStatuses = new ArrayList<>();

        int getJobStatusCalls;

        String downloadResultBody;

        int downloadResultCalls;

        List<EmbeddingModel> models = List.of();

        ConfigOptions configOptions;

        int initializeConfigCalls;

        int getConfigCalls;

        ProcessingOptions configDefaults;

        int getConfigDefaultsCalls;

        int updateConfigDefaultsCalls;

        int resetConfigDefaultsCalls;

        List<ConfigRule> configRules = List.of();

        ConfigRule lastConfigRule;

        String lastRuleId;

        int createConfigRuleCalls;

        int updateConfigRuleCalls;

        int deleteConfigRuleCalls;

        RuleTestResponse ruleTestResponse;

        int testConfigRulesCalls;

        HealthDetails healthDetails;

        int getHealthDetailsCalls;

        public TestDataCurationHttpClient() {
            super(DataCurationHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test").clientSecret("test")));
        }

        @Override
        public PresignResponse presign(ProcessingOptions options) {
            presignCalls++;
            lastPresignOptions = options;
            return presignResponse;
        }

        @Override
        public void upload(String putUrl, CICBlob blob) {
            uploadCalls.add(new UploadCall(putUrl, blob));
        }

        @Override
        public JobStatus getJobStatus(String jobId) {
            getJobStatusCalls++;
            if (!jobStatuses.isEmpty()) {
                return jobStatuses.remove(0);
            }
            return jobStatus;
        }

        @Override
        public String downloadResult(String getUrl) {
            downloadResultCalls++;
            return downloadResultBody;
        }

        @Override
        public List<EmbeddingModel> listModels() {
            return models;
        }

        @Override
        public ConfigOptions initializeConfig() {
            initializeConfigCalls++;
            return configOptions;
        }

        @Override
        public ConfigOptions getConfig() {
            getConfigCalls++;
            return configOptions;
        }

        @Override
        public ProcessingOptions getConfigDefaults() {
            getConfigDefaultsCalls++;
            return configDefaults;
        }

        @Override
        public ProcessingOptions updateConfigDefaults(ProcessingOptions defaults) {
            updateConfigDefaultsCalls++;
            return configDefaults;
        }

        @Override
        public void resetConfigDefaults() {
            resetConfigDefaultsCalls++;
        }

        @Override
        public List<ConfigRule> listConfigRules() {
            return configRules;
        }

        @Override
        public ConfigRule createConfigRule(ConfigRule rule) {
            createConfigRuleCalls++;
            return lastConfigRule;
        }

        @Override
        public ConfigRule getConfigRule(String ruleId) {
            lastRuleId = ruleId;
            return lastConfigRule;
        }

        @Override
        public ConfigRule updateConfigRule(String ruleId, ConfigRule rule) {
            lastRuleId = ruleId;
            updateConfigRuleCalls++;
            return lastConfigRule;
        }

        @Override
        public void deleteConfigRule(String ruleId) {
            lastRuleId = ruleId;
            deleteConfigRuleCalls++;
        }

        @Override
        public RuleTestResponse testConfigRules(RuleTestRequest testRequest) {
            testConfigRulesCalls++;
            return ruleTestResponse;
        }

        @Override
        public HealthDetails getHealthDetails() {
            getHealthDetailsCalls++;
            return healthDetails;
        }
    }
}
