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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
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
class KEHttpClientUploadTest {

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
    void uploadDoesNotRetryWhenPolicyIsNone() {
        var attemptCount = new AtomicInteger();
        server.createContext("/upload", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());

        assertThrows(CICSdkException.class, () -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(1, attemptCount.get());
    }

    @Test
    void uploadSendsContentTypeHeader() {
        var capturedContentType = new AtomicReference<String>();
        server.createContext("/upload", exchange -> {
            capturedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals("application/pdf", capturedContentType.get());
    }

    @Test
    void uploadReceivesFullBlobContentOnRetry() {
        byte[] expectedContent = "ke upload content".getBytes();
        var receivedBodies = new java.util.ArrayList<byte[]>();
        var attemptCount = new AtomicInteger();

        server.createContext("/upload", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            receivedBodies.add(exchange.getRequestBody().readAllBytes());
            if (attempt < 2) {
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

        CICBlob blob = new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(expectedContent);
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getContentType() {
                return Optional.of("image/jpeg");
            }
        };

        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", blob));
        assertEquals(2, attemptCount.get());
        for (var body : receivedBodies) {
            assertEquals(new String(expectedContent), new String(body));
        }
    }

    private KEHttpClient buildClient(RetryPolicy retryPolicy) {
        return KEHttpClient.from(baseUrl,
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
