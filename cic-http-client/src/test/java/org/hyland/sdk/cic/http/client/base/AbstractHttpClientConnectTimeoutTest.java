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
package org.hyland.sdk.cic.http.client.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * Tests timeout propagation from builder to {@link AbstractHttpClient}.
 *
 * @since 1.0.0
 */
class AbstractHttpClientConnectTimeoutTest {

    private HttpServer server;

    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    // ---- connectTimeout ----

    @Test
    void connectTimeoutIsNullByDefault() {
        var client = new TestHttpClient.Builder(baseUrl).build();
        assertNull(client.connectTimeout);
    }

    @Test
    void connectTimeoutIsStoredFromBuilder() {
        var timeout = Duration.ofSeconds(42);
        var client = new TestHttpClient.Builder(baseUrl).connectTimeout(timeout).build();
        assertEquals(timeout, client.connectTimeout);
    }

    // ---- requestTimeout ----

    @Test
    void requestTimeoutDefaultsToThirtySeconds() {
        var client = new TestHttpClient.Builder(baseUrl).build();
        assertNotNull(client.requestTimeout);
        assertEquals(Duration.ofSeconds(30), client.requestTimeout);
    }

    @Test
    void requestTimeoutIsStoredFromBuilder() {
        var timeout = Duration.ofSeconds(15);
        var client = new TestHttpClient.Builder(baseUrl).requestTimeout(timeout).build();
        assertEquals(timeout, client.requestTimeout);
    }

    @Test
    void requestTimeoutCanBeDisabled() {
        var client = new TestHttpClient.Builder(baseUrl).requestTimeout(null).build();
        assertNull(client.requestTimeout);
    }

    @Test
    void requestTimesOutWhenServerIsSlow() {
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl).requestTimeout(Duration.ofMillis(100)).build();

        assertThrows(CICSdkException.class, () -> client.doGet("/slow"));
    }

    @Test
    void requestSucceedsWithConfiguredRequestTimeout() {
        server.createContext("/fast", exchange -> {
            var body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl).requestTimeout(Duration.ofSeconds(5)).build();

        assertEquals(200, client.doGet("/fast").statusCode());
    }

    @Test
    void requestSucceedsWithDefaultRequestTimeout() {
        server.createContext("/fast", exchange -> {
            var body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        var client = new TestHttpClient.Builder(baseUrl).build();

        assertEquals(200, client.doGet("/fast").statusCode());
    }

    @Test
    void shortConnectTimeoutDoesNotAffectRequestResponseCycle() {
        server.createContext("/fast", exchange -> {
            var body = "ok".getBytes();
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        // short connectTimeout but generous requestTimeout — fast server should succeed
        var client = new TestHttpClient.Builder(baseUrl).connectTimeout(Duration.ofMillis(500))
                                                        .requestTimeout(Duration.ofSeconds(5))
                                                        .build();

        assertEquals(200, client.doGet("/fast").statusCode());
    }

    static class TestHttpClient extends AbstractHttpClient {

        final Duration connectTimeout;

        TestHttpClient(Builder builder) {
            super(builder);
            this.connectTimeout = builder.connectTimeout;
        }

        CICHttpResponse<String> doGet(String path) {
            return sendThenReadAsString(requestBuilder(CICHttpRequest.GET, path).build());
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
