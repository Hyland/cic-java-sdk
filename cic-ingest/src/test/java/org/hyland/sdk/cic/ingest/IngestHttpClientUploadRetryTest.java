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
package org.hyland.sdk.cic.ingest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
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
class IngestHttpClientUploadRetryTest {

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
    void uploadRetriesOnIOException() {
        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(2)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        // connect to a port that isn't listening - triggers IOException
        assertThrows(CICSdkException.class, () -> client.upload("http://localhost:1/upload", createTestBlob()));
    }

    @Test
    void uploadDoesNotRetryOnClientError() {
        var attemptCount = new AtomicInteger();
        server.createContext("/upload", exchange -> {
            attemptCount.incrementAndGet();
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.builder()
                                            .maxAttempts(3)
                                            .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ZERO))
                                            .build());

        assertThrows(CICSdkException.class, () -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(1, attemptCount.get());
    }

    @Test
    void uploadReceivesFullBlobContentOnEachRetry() {
        byte[] expectedContent = "test content for retry".getBytes();
        var receivedBodies = new java.util.ArrayList<byte[]>();
        var attemptCount = new AtomicInteger();

        server.createContext("/upload", exchange -> {
            int attempt = attemptCount.incrementAndGet();
            byte[] body = exchange.getRequestBody().readAllBytes();
            receivedBodies.add(body);
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

        CICBlob blob = new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(expectedContent);
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.of("sha256:abc123");
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }
        };

        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", blob));
        assertEquals(3, attemptCount.get());
        for (int i = 0; i < receivedBodies.size(); i++) {
            assertEquals(new String(expectedContent), new String(receivedBodies.get(i)),
                    "Attempt " + (i + 1) + " should receive full blob content");
        }
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
    void uploadSendsContentLengthHeader() {
        byte[] content = "hello world".getBytes();
        var capturedContentLength = new AtomicReference<String>();
        server.createContext("/upload", exchange -> {
            capturedContentLength.set(exchange.getRequestHeaders().getFirst("Content-Length"));
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        CICBlob blob = new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(content);
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public String getContentType() {
                return "application/octet-stream";
            }
        };

        assertDoesNotThrow(() -> client.upload(baseUrl + "/upload", blob));
        assertEquals(String.valueOf(content.length), capturedContentLength.get());
    }

    @Test
    void uploadThrowsOnEmptyBlob() {
        server.start();
        var client = buildClient(RetryPolicy.none());
        CICBlob emptyBlob = new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }
        };

        assertThrows(CICSdkException.class, () -> client.upload(baseUrl + "/upload", emptyBlob));
    }

    @Test
    void uploadDeletesTempFileAfterSuccess() throws IOException {
        server.createContext("/upload", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        long before = countCicTempFiles();
        client.upload(baseUrl + "/upload", createTestBlob());
        assertEquals(before, countCicTempFiles(), "No temp files should remain after successful upload");
    }

    @Test
    void uploadDeletesTempFileAfterFailure() throws IOException {
        server.createContext("/upload", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();

        var client = buildClient(RetryPolicy.none());
        long before = countCicTempFiles();
        assertThrows(CICSdkException.class, () -> client.upload(baseUrl + "/upload", createTestBlob()));
        assertEquals(before, countCicTempFiles(), "No temp files should remain after failed upload");
    }

    private long countCicTempFiles() throws IOException {
        try (var stream = Files.list(Path.of(System.getProperty("java.io.tmpdir")))) {
            return stream.filter(p -> p.getFileName().toString().startsWith("cic-upload-")).count();
        }
    }

    private IngestHttpClient buildClient(RetryPolicy retryPolicy) {
        return IngestHttpClient.from(baseUrl,
                AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret"))
                               .sourceId("test-source")
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
                return Optional.of("sha256:abc123");
            }

            @Override
            public String getContentType() {
                return "application/pdf";
            }
        };
    }
}
