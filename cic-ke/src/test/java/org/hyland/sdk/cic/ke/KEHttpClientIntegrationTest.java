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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * Integration tests for {@link KEHttpClient} exercising the full auth + API chain.
 *
 * @since 1.0.0
 */
class KEHttpClientIntegrationTest {

    private HttpServer authServer;

    private HttpServer apiServer;

    private KEHttpClient client;

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

    private KEHttpClient buildClient() {
        return KEHttpClient.from(TestHttpServers.baseUrl(apiServer),
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret"))
                           .retryPolicy(RetryPolicy.none())
                           .build();
    }

    // --- getPresignedUrl ---

    @Test
    void getPresignedUrlReturnsUrlAndObjectKey() {
        var capturedAuth = new AtomicReference<String>();
        apiServer.createContext("/files/upload/presigned-url", exchange -> {
            capturedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            TestHttpServers.respondJson(exchange, 200, """
                    {"presignedUrl":"https://upload.example.com/signed","objectKey":"contents/doc.pdf"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var result = client.getPresignedUrl("application/pdf");

        assertNotNull(result);
        assertEquals("https://upload.example.com/signed", result.presignedUrl());
        assertEquals("contents/doc.pdf", result.objectKey());
        assertEquals("Bearer test-token", capturedAuth.get());
    }

    // --- process ---

    @Test
    void processReturnsProcessingId() {
        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/content/process", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {"processingId":"proc-123"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var request = ProcessRequest.builder().objectKey("contents/doc.pdf").action(Action.TEXT_SUMMARIZATION).build();
        var processingId = client.process(request);

        assertEquals("proc-123", processingId);
        assertTrue(capturedBody.get().contains("textSummarization"));
        assertTrue(capturedBody.get().contains("contents/doc.pdf"));
        assertTrue(capturedBody.get().contains("context.api/v2"));
    }

    // --- getResults ---

    @Test
    void getResultsReturnsEnrichmentResult() {
        apiServer.createContext("/content/process/proc-1/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {
                      "id":"proc-1","timestamp":"2026-01-01T00:00:00Z","status":"SUCCESS","inProgress":false,
                      "results":[{
                        "objectKey":"contents/doc.pdf",
                        "textSummary":{"isSuccess":true,"result":"A summary","error":null}
                      }]
                    }
                    """);
        });
        apiServer.start();
        client = buildClient();

        var result = client.getResults("proc-1");

        assertNotNull(result);
        assertEquals("proc-1", result.id());
        assertTrue(result.isSuccess());
        assertEquals(1, result.results().size());
        assertEquals("A summary", result.results().get(0).textSummary().result());
    }

    // --- getResultsIfReady ---

    @Test
    void getResultsIfReadyReturnsNullOn202() {
        apiServer.createContext("/content/process/proc-2/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondEmpty(exchange, 202);
        });
        apiServer.start();
        client = buildClient();

        var result = client.getResultsIfReady("proc-2");

        assertNull(result);
    }

    @Test
    void getResultsIfReadyReturnsResultOn200() {
        apiServer.createContext("/content/process/proc-3/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200,
                    """
                            {"id":"proc-3","timestamp":"2026-01-01T00:00:00Z","status":"SUCCESS","inProgress":false,"results":[]}
                            """);
        });
        apiServer.start();
        client = buildClient();

        var result = client.getResultsIfReady("proc-3");

        assertNotNull(result);
        assertEquals("proc-3", result.id());
    }

    @Test
    void getResultsIfReadyPolling202Then200() {
        var attemptCount = new AtomicInteger();
        apiServer.createContext("/content/process/proc-4/results", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 3) {
                TestHttpServers.respondEmpty(exchange, 202);
            } else {
                TestHttpServers.respondJson(exchange, 200,
                        """
                                {"id":"proc-4","timestamp":"2026-01-01T00:00:00Z","status":"SUCCESS","inProgress":false,"results":[]}
                                """);
            }
        });
        apiServer.start();
        client = buildClient();

        assertNull(client.getResultsIfReady("proc-4"));
        assertNull(client.getResultsIfReady("proc-4"));
        var result = client.getResultsIfReady("proc-4");
        assertNotNull(result);
        assertEquals(3, attemptCount.get());
    }

    // --- getActions ---

    @Test
    void getActionsReturnsJsonString() {
        apiServer.createContext("/content/actions", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    ["textSummarization","imageDescription","namedEntityRecognitionText"]
                    """);
        });
        apiServer.start();
        client = buildClient();

        var actions = client.getActions();

        assertNotNull(actions);
        assertTrue(actions.contains("textSummarization"));
        assertTrue(actions.contains("imageDescription"));
    }

    // --- isHealthy ---

    @Test
    void isHealthyReturnsTrueOn200() {
        apiServer.createContext("/healthy", exchange -> {
            TestHttpServers.respondEmpty(exchange, 200);
        });
        apiServer.start();
        client = buildClient();

        assertTrue(client.isHealthy());
    }

    @Test
    void isHealthyReturnsFalseOnServerError() {
        apiServer.createContext("/healthy", exchange -> {
            TestHttpServers.respondEmpty(exchange, 503);
        });
        apiServer.start();
        client = buildClient();

        assertFalse(client.isHealthy());
    }

    // --- Error handling ---

    @Test
    void getPresignedUrlThrowsOnServerError() {
        apiServer.createContext("/files/upload/presigned-url", exchange -> {
            TestHttpServers.respondJson(exchange, 500, """
                    {"error":"Internal Server Error"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        assertThrows(CICSdkException.class, () -> client.getPresignedUrl("application/pdf"));
    }

    @Test
    void processThrowsOnBadRequest() {
        apiServer.createContext("/content/process", exchange -> {
            TestHttpServers.respondJson(exchange, 400, """
                    {"error":"Bad Request"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var request = ProcessRequest.builder().objectKey("key").action("textEmbeddings").build();
        assertThrows(CICSdkException.class, () -> client.process(request));
    }
}
