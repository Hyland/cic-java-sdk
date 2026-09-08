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
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;

/**
 * End-to-end workflow integration tests exercising {@link KEService} and {@link DataCurationService} against dual JDK
 * HttpServers (auth + API).
 *
 * @since 1.0.0
 */
class KEWorkflowIntegrationTest {

    private HttpServer authServer;

    private HttpServer apiServer;

    @BeforeEach
    void setUp() throws IOException {
        authServer = TestHttpServers.createAuthServer();
        authServer.start();

        apiServer = HttpServer.create(new InetSocketAddress(0), 0);
    }

    @AfterEach
    void tearDown() {
        if (apiServer != null) {
            apiServer.stop(0);
        }
        if (authServer != null) {
            authServer.stop(0);
        }
    }

    // =========================================================================
    // KE Context API workflow: presign -> upload -> process -> poll -> results
    // =========================================================================

    @Test
    void keEnrichmentEndToEndWorkflow() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        apiServer.createContext("/files/upload/presigned-url", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            String uploadTarget = apiBase + "/upload-target";
            TestHttpServers.respondJson(exchange, 200, """
                    {"presignedUrl":"%s","objectKey":"contents/uploaded-doc.pdf"}
                    """.formatted(uploadTarget));
        });

        var uploadReceived = new AtomicInteger();
        apiServer.createContext("/upload-target", exchange -> {
            if ("PUT".equals(exchange.getRequestMethod())) {
                exchange.getRequestBody().readAllBytes();
                uploadReceived.incrementAndGet();
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        });

        apiServer.createContext("/content/process", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.readRequestBody(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"processingId":"wf-proc-001"}
                    """);
        });

        var pollCount = new AtomicInteger();
        apiServer.createContext("/content/process/wf-proc-001/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            int attempt = pollCount.incrementAndGet();
            if (attempt < 2) {
                TestHttpServers.respondEmpty(exchange, 202);
            } else {
                TestHttpServers.respondJson(exchange, 200, """
                        {
                          "id":"wf-proc-001","timestamp":"2026-01-01T00:00:00Z","status":"SUCCESS","inProgress":false,
                          "results":[{
                            "objectKey":"contents/uploaded-doc.pdf",
                            "textSummary":{"isSuccess":true,"result":"This is a summary.","error":null}
                          }]
                        }
                        """);
            }
        });

        apiServer.start();

        var keClient = KEHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret"))
                                   .retryPolicy(RetryPolicy.none())
                                   .build();

        var keService = new KEService(keClient) {
            @Override
            protected void sleep(long millis) {
                // no-op for fast tests
            }
        };
        keService.setPollSettings(5, Duration.ofMillis(100));

        CICBlob blob = createTestBlob("application/pdf", "PDF content bytes");

        var result = keService.enrich(blob, List.of(Action.TEXT_SUMMARIZATION));

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("wf-proc-001", result.id());
        assertEquals(1, result.results().size());
        assertEquals("This is a summary.", result.results().get(0).textSummary().result());
        assertEquals(1, uploadReceived.get());
        assertEquals(2, pollCount.get());
    }

    @Test
    void keSendForEnrichmentThenPollSeparately() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        apiServer.createContext("/files/upload/presigned-url", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"presignedUrl":"%s","objectKey":"contents/img.jpg"}
                    """.formatted(apiBase + "/upload-target"));
        });

        apiServer.createContext("/upload-target", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/content/process", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {"processingId":"sep-proc-002"}
                    """);
        });

        apiServer.createContext("/content/process/sep-proc-002/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200,
                    """
                            {"id":"sep-proc-002","timestamp":"2026-01-01T00:00:00Z","status":"SUCCESS","inProgress":false,"results":[]}
                            """);
        });

        apiServer.start();

        var keClient = KEHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret"))
                                   .retryPolicy(RetryPolicy.none())
                                   .build();
        var keService = new KEService(keClient) {
            @Override
            protected void sleep(long millis) {
                // no-op
            }
        };

        CICBlob blob = createTestBlob("image/jpeg", "JPEG data");
        String processingId = keService.sendForEnrichment(blob,
                builder -> builder.action(Action.IMAGE_DESCRIPTION, cfg -> cfg.maxWordCount(100)));
        assertEquals("sep-proc-002", processingId);

        assertTrue(capturedBody.get().contains("\"version\":\"context.api/v2\""));
        assertTrue(capturedBody.get().contains("\"imageDescription\""));

        var result = keService.pollResults(processingId);
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void kePollTimesOutWhenAlways202() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        apiServer.createContext("/content/process/timeout-proc/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondEmpty(exchange, 202);
        });

        apiServer.start();

        var keClient = KEHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret"))
                                   .retryPolicy(RetryPolicy.none())
                                   .build();
        var keService = new KEService(keClient) {
            @Override
            protected void sleep(long millis) {
                // no-op
            }
        };
        keService.setPollSettings(3, Duration.ofMillis(10));

        assertThrows(CICSdkException.class, () -> keService.pollResults("timeout-proc"));
    }

    // =========================================================================
    // Data Curation workflow: presign -> upload -> poll status -> download
    // =========================================================================

    @Test
    void dataCurationEndToEndWorkflow() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        apiServer.createContext("/presign", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"job_id":"dc-job-001","put_url":"%s/dc-upload","get_url":"%s/dc-download"}
                    """.formatted(apiBase, apiBase));
        });

        var dcUploadReceived = new AtomicInteger();
        apiServer.createContext("/dc-upload", exchange -> {
            if ("PUT".equals(exchange.getRequestMethod())) {
                exchange.getRequestBody().readAllBytes();
                dcUploadReceived.incrementAndGet();
                exchange.sendResponseHeaders(200, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        });

        var statusPollCount = new AtomicInteger();
        apiServer.createContext("/status/dc-job-001", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            int attempt = statusPollCount.incrementAndGet();
            if (attempt < 2) {
                TestHttpServers.respondJson(exchange, 200, """
                        {"jobId":"dc-job-001","status":"Processing"}
                        """);
            } else {
                TestHttpServers.respondJson(exchange, 200, """
                        {"jobId":"dc-job-001","status":"Done"}
                        """);
            }
        });

        apiServer.createContext("/dc-download", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                var resultJson = """
                        {"chunks":[{"text":"Chunk 1","embedding":[0.1,0.2]},{"text":"Chunk 2","embedding":[0.3,0.4]}]}
                        """;
                var bytes = resultJson.getBytes();
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        });

        apiServer.start();

        var dcClient = DataCurationHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret")).retryPolicy(RetryPolicy.none()).build();

        var dcService = new DataCurationService(dcClient) {
            @Override
            protected void sleep(long millis) {
                // no-op for fast tests
            }
        };
        dcService.setPollSettings(5, Duration.ofMillis(100));

        CICBlob blob = createTestBlob("application/pdf", "Some PDF content");

        var resultJson = dcService.curate(blob, (ProcessingOptions) null);

        assertNotNull(resultJson);
        assertTrue(resultJson.contains("Chunk 1"));
        assertTrue(resultJson.contains("Chunk 2"));
        assertEquals(1, dcUploadReceived.get());
        assertEquals(2, statusPollCount.get());
    }

    @Test
    void dataCurationWithProcessingOptions() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        var capturedPresignBody = new AtomicReference<String>();
        apiServer.createContext("/presign", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedPresignBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {"job_id":"dc-job-002","put_url":"%s/dc-upload","get_url":"%s/dc-download"}
                    """.formatted(apiBase, apiBase));
        });

        apiServer.createContext("/dc-upload", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        apiServer.createContext("/status/dc-job-002", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"jobId":"dc-job-002","status":"Done"}
                    """);
        });

        apiServer.createContext("/dc-download", exchange -> {
            var body = """
                    {"result":"curated"}
                    """.getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        apiServer.start();

        var dcClient = DataCurationHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret")).retryPolicy(RetryPolicy.none()).build();
        var dcService = new DataCurationService(dcClient) {
            @Override
            protected void sleep(long millis) {
                // no-op
            }
        };

        CICBlob blob = createTestBlob("application/pdf", "content");

        var result = dcService.curate(blob, opts -> opts.chunking(true).chunkSize(1500).embedding(true));

        assertNotNull(result);
        assertTrue(result.contains("curated"));
        assertTrue(capturedPresignBody.get().contains("chunking"));
        assertTrue(capturedPresignBody.get().contains("1500"));
    }

    @Test
    void dataCurationPollTimesOut() {
        String apiBase = TestHttpServers.baseUrl(apiServer);

        apiServer.createContext("/presign", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"job_id":"dc-timeout","put_url":"%s/dc-upload","get_url":"%s/dc-download"}
                    """.formatted(apiBase, apiBase));
        });

        apiServer.createContext("/dc-upload", exchange -> {
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        apiServer.createContext("/status/dc-timeout", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"jobId":"dc-timeout","status":"Processing"}
                    """);
        });

        apiServer.start();

        var dcClient = DataCurationHttpClient.from(apiBase,
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret")).retryPolicy(RetryPolicy.none()).build();
        var dcService = new DataCurationService(dcClient) {
            @Override
            protected void sleep(long millis) {
                // no-op
            }
        };
        dcService.setPollSettings(2, Duration.ofMillis(10));

        CICBlob blob = createTestBlob("application/pdf", "content");

        assertThrows(CICSdkException.class, () -> dcService.curate(blob, (ProcessingOptions) null));
    }

    private CICBlob createTestBlob(String contentType, String content) {
        return new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(content.getBytes());
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
