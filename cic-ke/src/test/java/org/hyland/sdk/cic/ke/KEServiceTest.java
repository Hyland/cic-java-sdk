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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ActionDescriptor;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * @since 1.0.0
 */
class KEServiceTest {

    private TestKEHttpClient httpClient;

    private KEService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestKEHttpClient();
        service = new KEService(httpClient);
        service.setPollSettings(3, 10);
    }

    @Test
    void testGetPresignedUrl() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");

        var result = service.getPresignedUrl("application/pdf");

        assertNotNull(result);
        assertEquals("contents/file.pdf", result.objectKey());
    }

    @Test
    void testProcess() {
        httpClient.processResult = "processing-id-123";

        var request = ProcessRequest.builder()
                                    .objectPath("contents/file.pdf")
                                    .action(Action.TEXT_SUMMARIZATION)
                                    .build();

        var processingId = service.process(request);

        assertEquals("processing-id-123", processingId);
        assertEquals(1, httpClient.processCalls.size());
    }

    @Test
    void testSendForEnrichment() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");
        httpClient.processResult = "processing-id-456";

        CICBlob blob = createTestBlob();
        var processingId = service.sendForEnrichment(blob, builder -> builder.action(Action.TEXT_SUMMARIZATION));

        assertEquals("processing-id-456", processingId);
        assertEquals(1, httpClient.getPresignedUrlCalls);
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals(1, httpClient.processCalls.size());
        assertEquals(1, httpClient.processCalls.get(0).objectKeys().size());
        assertEquals("contents/file.pdf", httpClient.processCalls.get(0).objectKeys().get(0).path());
    }

    @Test
    void testSendForEnrichmentValidatesRequestBeforeUpload() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");

        var exception = assertThrows(IllegalArgumentException.class,
                () -> service.sendForEnrichment(createTestBlob(), builder -> {
                    // no actions configured
                }));

        assertEquals("At least one action is required", exception.getMessage());
        assertEquals(1, httpClient.getPresignedUrlCalls);
        assertTrue(httpClient.uploadCalls.isEmpty());
        assertTrue(httpClient.processCalls.isEmpty());
    }

    @Test
    void testEnrichEndToEnd() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");
        httpClient.processResult = "processing-id-789";
        httpClient.enrichmentResult = new EnrichmentResult("processing-id-789", "2026-01-01T00:00:00Z", List.of(),
                "SUCCESS", false);

        CICBlob blob = createTestBlob();
        var result = service.enrich(blob, List.of(Action.TEXT_SUMMARIZATION));

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("processing-id-789", result.id());
    }

    @Test
    void testEnrichWithPolling() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");
        httpClient.processResult = "processing-id-poll";
        httpClient.resultsSequence.add(null);
        httpClient.resultsSequence.add(
                new EnrichmentResult("processing-id-poll", "2026-01-01T00:00:00Z", List.of(), "SUCCESS", false));

        CICBlob blob = createTestBlob();
        var result = service.enrich(blob, List.of(Action.TEXT_SUMMARIZATION));

        assertNotNull(result);
        assertEquals(2, httpClient.getResultsIfReadyCalls);
    }

    @Test
    void testPollResultsTimeout() {
        httpClient.enrichmentResult = null;

        assertThrows(CICSdkException.class, () -> service.pollResults("processing-id-timeout"));
    }

    // --- Consumer overloads ---

    @Test
    void testProcessWithConsumer() {
        httpClient.processResult = "consumer-proc-id";

        var processingId = service.process(
                req -> req.objectPath("contents/file.pdf").action(Action.TEXT_SUMMARIZATION));

        assertEquals("consumer-proc-id", processingId);
        assertEquals(1, httpClient.processCalls.size());
        assertEquals("contents/file.pdf", httpClient.processCalls.get(0).objectKeys().get(0).path());
    }

    @Test
    void testEnrichWithConsumer() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");
        httpClient.processResult = "consumer-enrich-id";
        httpClient.enrichmentResult = new EnrichmentResult("consumer-enrich-id", "2026-01-01T00:00:00Z", List.of(),
                "SUCCESS", false);

        CICBlob blob = createTestBlob();
        var result = service.enrich(blob, req -> req.action(Action.IMAGE_DESCRIPTION, cfg -> cfg.maxWordCount(100)));

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    // --- v2 action config propagation ---

    @Test
    void testSendForEnrichmentPropagatesActionConfigs() {
        httpClient.presignedUrl = new PresignedUrl("https://upload.url", "contents/file.pdf");
        httpClient.processResult = "cfg-proc-id";

        CICBlob blob = createTestBlob();
        service.sendForEnrichment(blob,
                builder -> builder.action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(200))
                                  .action(Action.TEXT_CLASSIFICATION,
                                          cfg -> cfg.classes(List.of("a", "b"))
                                                    .instructions(Map.of("context", "legal"))));

        assertEquals(1, httpClient.processCalls.size());
        var submitted = httpClient.processCalls.get(0);
        assertEquals(1, submitted.objectKeys().size());
        assertEquals("contents/file.pdf", submitted.objectKeys().get(0).path());
        assertEquals(2, submitted.actions().size());
        assertEquals(200, submitted.actions().get("textSummarization").maxWordCount());
        assertEquals(List.of("a", "b"), submitted.actions().get("textClassification").classes());
        assertEquals("legal", submitted.actions().get("textClassification").instructions().get("context"));
    }

    // --- Delegate and null validation ---

    @Test
    void testUpload() {
        CICBlob blob = createTestBlob();
        service.upload("https://presigned.url", blob);

        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals("https://presigned.url", httpClient.uploadCalls.get(0).url());
    }

    @Test
    void testUploadNullUrlThrows() {
        assertThrows(NullPointerException.class, () -> service.upload(null, createTestBlob()));
    }

    @Test
    void testUploadNullBlobThrows() {
        assertThrows(NullPointerException.class, () -> service.upload("https://presigned.url", null));
    }

    @Test
    void testProcessNullThrows() {
        assertThrows(NullPointerException.class, () -> service.process((ProcessRequest) null));
    }

    @Test
    void testGetResultsNullThrows() {
        assertThrows(NullPointerException.class, () -> service.getResults(null));
    }

    @Test
    void testGetPresignedUrlNullThrows() {
        assertThrows(NullPointerException.class, () -> service.getPresignedUrl(null));
    }

    @Test
    void testSetPollSettingsRejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(0, 1000));
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(-1, 1000));
        assertThrows(IllegalArgumentException.class, () -> service.setPollSettings(5, -1));
    }

    @Test
    void testGetActionDescriptors() {
        var descriptors = service.getActionDescriptors();
        assertNotNull(descriptors);
        assertEquals(2, descriptors.size());
        assertEquals("textSummarization", descriptors.get(0).name());
        assertEquals("pretrainedClassification", descriptors.get(1).name());
        assertEquals(List.of("model-a"), descriptors.get(1).availableModels());
        assertEquals(List.of("cat-x"), descriptors.get(1).availableCategories());
    }

    @Test
    void testIsHealthy() {
        assertTrue(service.isHealthy());
    }

    @Test
    void testGetVersionUsageStats() {
        var stats = service.getVersionUsageStats();
        assertNotNull(stats);
        assertEquals(58L, stats.get("v2"));
        assertEquals(6L, stats.get("v1"));
    }

    @Test
    void testGetResults() {
        httpClient.enrichmentResult = new EnrichmentResult("res-1", "2026-01-01T00:00:00Z", List.of(), "SUCCESS",
                false);

        var result = service.getResults("res-1");

        assertNotNull(result);
        assertEquals("res-1", result.id());
    }

    @Test
    void testPollResultsNullThrows() {
        assertThrows(NullPointerException.class, () -> service.pollResults(null));
    }

    @Test
    void testSendForEnrichmentNullBlobThrows() {
        assertThrows(NullPointerException.class,
                () -> service.sendForEnrichment(null, builder -> builder.action(Action.TEXT_EMBEDDINGS)));
    }

    @Test
    void testSendForEnrichmentNullConsumerThrows() {
        assertThrows(NullPointerException.class, () -> service.sendForEnrichment(createTestBlob(), null));
    }

    @Test
    void testEnrichNullBlobThrows() {
        assertThrows(NullPointerException.class, () -> service.enrich(null, List.of(Action.TEXT_SUMMARIZATION)));
    }

    @Test
    void testEnrichNullActionsThrows() {
        assertThrows(NullPointerException.class, () -> service.enrich(createTestBlob(), (List<Action>) null));
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

    private static class TestKEHttpClient extends KEHttpClient {

        PresignedUrl presignedUrl;

        int getPresignedUrlCalls;

        List<UploadCall> uploadCalls = new ArrayList<>();

        List<ProcessRequest> processCalls = new ArrayList<>();

        String processResult;

        EnrichmentResult enrichmentResult;

        List<EnrichmentResult> resultsSequence = new ArrayList<>();

        int getResultsIfReadyCalls;

        public TestKEHttpClient() {
            super(KEHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test").clientSecret("test")));
        }

        @Override
        public PresignedUrl getPresignedUrl(String contentType) {
            getPresignedUrlCalls++;
            return presignedUrl;
        }

        @Override
        public void upload(String presignedUrl, CICBlob blob) {
            uploadCalls.add(new UploadCall(presignedUrl, blob));
        }

        @Override
        public String process(ProcessRequest processRequest) {
            processCalls.add(processRequest);
            return processResult;
        }

        @Override
        public EnrichmentResult getResults(String processingId) {
            return enrichmentResult;
        }

        @Override
        public EnrichmentResult getResultsIfReady(String processingId) {
            getResultsIfReadyCalls++;
            if (!resultsSequence.isEmpty()) {
                return resultsSequence.remove(0);
            }
            return enrichmentResult;
        }

        @Override
        public List<ActionDescriptor> getActionDescriptors() {
            return List.of(new ActionDescriptor("textSummarization", null, null),
                    new ActionDescriptor("pretrainedClassification", List.of("model-a"), List.of("cat-x")));
        }

        @Override
        public boolean isHealthy() {
            return true;
        }

        @Override
        public Map<String, Long> getVersionUsageStats() {
            return Map.of("v2", 58L, "v1", 6L);
        }
    }
}
