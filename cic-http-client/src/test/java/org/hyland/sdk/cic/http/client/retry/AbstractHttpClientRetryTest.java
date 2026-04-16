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
 *     Damian Ujma
 */
package org.hyland.sdk.cic.http.client.retry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClient;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClientBuilder;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest;
import org.hyland.sdk.cic.http.client.base.CICHttpResponse;

/**
 * Tests retry behavior integrated into {@link AbstractHttpClient}.
 *
 * @since 1.0.0
 */
public class AbstractHttpClientRetryTest {

    private HttpServer server;

    private String baseUrl;

    @BeforeEach
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void retriesOnServerError() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 3) {
                exchange.sendResponseHeaders(500, 0);
            } else {
                var body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doGet("/test");
        assertEquals(200, response.statusCode());
        assertEquals(3, attemptCount.get());
    }

    @Test
    public void doesNotRetryOnClientError() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            var body = "bad request".getBytes();
            exchange.sendResponseHeaders(400, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doGet("/test");
        assertEquals(400, response.statusCode());
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void doesNotRetryWhenPolicyIsNone() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl).retryPolicy(RetryPolicy.none()).build();

        var response = client.doGet("/test");
        assertEquals(500, response.statusCode());
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void returnsLastErrorResponseWhenRetriesExhausted() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            var body = "service unavailable".getBytes();
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(2)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doGet("/test");
        assertEquals(503, response.statusCode());
        assertEquals(2, attemptCount.get());
    }

    @Test
    public void customRetryConditionIsRespected() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(500, 0);
            exchange.close();
        });
        server.start();

        // only retry on 503, not on 500
        var client = new TestHttpClient.Builder(baseUrl).retryPolicy(
                RetryPolicy.builder()
                           .maxAttempts(3)
                           .retryCondition(ctx -> ctx.statusCode() == 503)
                           .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                           .build()).build();

        var response = client.doGet("/test");
        assertEquals(500, response.statusCode());
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void retriesOn429TooManyRequests() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt == 1) {
                exchange.sendResponseHeaders(429, 0);
            } else {
                var body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doGet("/test");
        assertEquals(200, response.statusCode());
        assertEquals(2, attemptCount.get());
    }

    @Test
    public void doesNotRetryOnIOExceptionWhenConditionReturnsFalse() {
        var conditionCallCount = new AtomicInteger();
        var client = new TestHttpClient.Builder("http://localhost:1").retryPolicy(
                RetryPolicy.builder().maxAttempts(3).retryCondition(ctx -> {
                    conditionCallCount.incrementAndGet();
                    return false;
                }).backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO)).build())
                                                                     .connectTimeout(Duration.ofMillis(100))
                                                                     .build();

        assertThrows(CICSdkException.class, () -> client.doGet("/test"));
        assertEquals(1, conditionCallCount.get());
    }

    @Test
    public void throwsAfterExhaustingRetriesOnIOException() {
        var client = new TestHttpClient.Builder("http://localhost:1").retryPolicy(
                RetryPolicy.builder().maxAttempts(2).backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO)).build())
                                                                     .connectTimeout(Duration.ofMillis(100))
                                                                     .build();

        assertThrows(CICSdkException.class, () -> client.doGet("/test"));
    }

    @Test
    public void successOnFirstAttemptDoesNotRetry() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            var body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doGet("/test");
        assertEquals(200, response.statusCode());
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void backoffDelayIsApplied() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 2) {
                exchange.sendResponseHeaders(500, 0);
            } else {
                var body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(2)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ofMillis(200)))
                                                                           .build())
                                                        .build();

        long start = System.currentTimeMillis();
        var response = client.doGet("/test");
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(200, response.statusCode());
        assertEquals(2, attemptCount.get());
        assertTrue(elapsed >= 150, "Expected backoff delay of ~200ms, but elapsed was " + elapsed + "ms");
    }

    @Test
    public void negativeDelayFromCustomStrategyIsClamped() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 2) {
                exchange.sendResponseHeaders(500, 0);
            } else {
                var body = "ok".getBytes();
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();

        // A custom strategy returning a negative duration must not throw or mask the original failure
        var client = new TestHttpClient.Builder(baseUrl).retryPolicy(
                RetryPolicy.builder().maxAttempts(2).backoffStrategy(ctx -> Duration.ofMillis(-500)).build()).build();

        var response = client.doGet("/test");
        assertEquals(200, response.statusCode());
        assertEquals(2, attemptCount.get());
    }

    @Test
    public void doesNotRetryPostOnIOException() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            // Close without sending response headers — causes IOException on the client side
            exchange.close();
        });
        server.start();

        // Default policy: POST + IOException should not retry
        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        assertThrows(CICSdkException.class, () -> client.doPost("/test"));
        assertEquals(1, attemptCount.get());
    }

    @Test
    public void doesNotRetryPostOnRetryableStatusCode() {
        var attemptCount = new AtomicInteger();
        server.createContext("/test", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(503, 0);
            exchange.close();
        });
        server.start();

        // Default policy: POST + 503 should not retry — non-idempotent methods are never retried
        var client = new TestHttpClient.Builder(baseUrl)
                                                        .retryPolicy(
                                                                RetryPolicy.builder()
                                                                           .maxAttempts(3)
                                                                           .backoffStrategy(BackoffStrategy.fixedDelay(
                                                                                   Duration.ZERO))
                                                                           .build())
                                                        .build();

        var response = client.doPost("/test");
        assertEquals(503, response.statusCode());
        assertEquals(1, attemptCount.get());
    }

    static class TestHttpClient extends AbstractHttpClient {

        TestHttpClient(Builder builder) {
            super(builder);
        }

        public CICHttpResponse<String> doGet(String path) {
            var request = requestBuilder(CICHttpRequest.GET, path).build();
            return sendThenReadAsString(request);
        }

        public CICHttpResponse<String> doPost(String path) {
            var request = requestBuilder(CICHttpRequest.POST, path).build();
            return sendThenReadAsString(request);
        }

        static class Builder extends AbstractHttpClientBuilder<Builder, TestHttpClient> {

            Builder(String baseUrl) {
                super(baseUrl);
            }

            @Override
            public TestHttpClient build() {
                return new TestHttpClient(this);
            }
        }
    }
}
