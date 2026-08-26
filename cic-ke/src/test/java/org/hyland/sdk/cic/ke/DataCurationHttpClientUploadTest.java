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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import org.hyland.sdk.cic.http.client.retry.BackoffStrategy;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;

/**
 * @since 1.0.0
 */
class DataCurationHttpClientUploadTest {

    private HttpServer server;

    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    // --- Upload tests ---

    @Test
    void uploadSucceedsOnFirstAttempt() {
        var attemptCount = new AtomicInteger();
        server.createContext("/upload", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(3)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(1, attemptCount.get());
    }

    @Test
    void uploadRetriesOnServerError() {
        var attemptCount = new AtomicInteger();
        server.createContext("/upload", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 3) {
                exchange.sendResponseHeaders(500, -1);
            } else {
                exchange.sendResponseHeaders(200, -1);
            }
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(3)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(3, attemptCount.get());
    }

    @Test
    void uploadThrowsAfterExhaustingRetries() {
        var attemptCount = new AtomicInteger();
        server.createContext("/upload", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(503, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(2)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        assertThrows(CICSdkException.class, () -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(2, attemptCount.get());
    }

    @Test
    void uploadAlwaysSendsOctetStreamContentType() {
        var capturedContentType = new AtomicReference<String>();
        server.createContext("/upload", exchange -> {
            capturedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals("application/octet-stream", capturedContentType.get());
    }

    // --- Download result tests ---

    @Test
    void downloadResultSucceeds() {
        var responseBody = "{\"markdown\":{\"output\":\"Hello world\"}}";
        server.createContext("/download", exchange -> {
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        var result = client.downloadResult(baseUrl + "/download");

        assertNotNull(result);
        assertTrue(result.contains("Hello world"));
    }

    @Test
    void downloadResultRetriesOnServerError() {
        var attemptCount = new AtomicInteger();
        var responseBody = "{\"result\":\"ok\"}";
        server.createContext("/download", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt < 2) {
                exchange.sendResponseHeaders(500, -1);
            } else {
                byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(3)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        var result = client.downloadResult(baseUrl + "/download");

        assertNotNull(result);
        assertEquals(2, attemptCount.get());
    }

    @Test
    void downloadResultThrowsAfterExhaustingRetries() {
        var attemptCount = new AtomicInteger();
        server.createContext("/download", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(2)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        assertThrows(CICSdkException.class, () -> client.downloadResult(baseUrl + "/download"));
        assertEquals(2, attemptCount.get());
    }

    private DataCurationHttpClient buildClient(RetryPolicy retryPolicy) {
        return DataCurationHttpClient.from(baseUrl,
                AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret"))
                                     .retryPolicy(retryPolicy)
                                     .build();
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
}
