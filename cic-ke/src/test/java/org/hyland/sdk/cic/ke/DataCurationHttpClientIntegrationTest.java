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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;

/**
 * Integration tests for {@link DataCurationHttpClient} exercising the full auth + API chain.
 *
 * @since 1.0.0
 */
class DataCurationHttpClientIntegrationTest {

    private HttpServer authServer;

    private HttpServer apiServer;

    private DataCurationHttpClient client;

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

    private DataCurationHttpClient buildClient() {
        return DataCurationHttpClient.from(TestHttpServers.baseUrl(apiServer),
                AuthenticationHttpClient.from(TestHttpServers.baseUrl(authServer))
                                        .clientId("test-client-id")
                                        .clientSecret("test-client-secret"))
                                     .retryPolicy(RetryPolicy.none())
                                     .build();
    }

    // --- Pipeline endpoints ---

    @Test
    void presignReturnsPresignResponse() {
        var capturedAuth = new AtomicReference<String>();
        apiServer.createContext("/presign", exchange -> {
            capturedAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            TestHttpServers.respondJson(exchange, 200, """
                    {"job_id":"job-1","put_url":"https://s3.example.com/put","get_url":"https://s3.example.com/get"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var response = client.presign(null);

        assertNotNull(response);
        assertEquals("job-1", response.jobId());
        assertEquals("https://s3.example.com/put", response.putUrl());
        assertEquals("https://s3.example.com/get", response.getUrl());
        assertEquals("Bearer test-token", capturedAuth.get());
    }

    @Test
    void presignWithOptionsSendsBody() {
        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/presign", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {"job_id":"job-2","put_url":"https://put","get_url":"https://get"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var options = ProcessingOptions.builder().chunking(true).chunkSize(2000).build();
        var response = client.presign(options);

        assertNotNull(response);
        assertTrue(capturedBody.get().contains("chunking"));
        assertTrue(capturedBody.get().contains("2000"));
    }

    @Test
    void getJobStatusReturnsStatus() {
        apiServer.createContext("/status/job-1", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    {"jobId":"job-1","status":"Done"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var status = client.getJobStatus("job-1");

        assertNotNull(status);
        assertEquals("job-1", status.jobId());
        assertTrue(status.isDone());
    }

    @Test
    void listModelsReturnsModels() {
        apiServer.createContext("/models", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            TestHttpServers.respondJson(exchange, 200, """
                    [{"id":"m1","name":"Model One"},{"id":"m2","name":"Model Two"}]
                    """);
        });
        apiServer.start();
        client = buildClient();

        var models = client.listModels();

        assertEquals(2, models.size());
        assertEquals("m1", models.get(0).id());
        assertEquals("m2", models.get(1).id());
    }

    @Test
    void isHealthyReturnsTrueOn200() {
        apiServer.createContext("/health", exchange -> {
            TestHttpServers.respondEmpty(exchange, 200);
        });
        apiServer.start();
        client = buildClient();

        assertTrue(client.isHealthy());
    }

    @Test
    void isHealthyReturnsFalseOnServerError() {
        apiServer.createContext("/health", exchange -> {
            TestHttpServers.respondEmpty(exchange, 503);
        });
        apiServer.start();
        client = buildClient();

        assertFalse(client.isHealthy());
    }

    // --- Config endpoints ---

    @Test
    void initializeConfigReturnsConfigOptions() {
        apiServer.createContext("/config/options", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("POST", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200, """
                    {"defaults":{"chunking":true,"chunk_size":1000},"rules":[]}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var config = client.initializeConfig();

        assertNotNull(config);
        assertEquals(true, config.defaults().chunking());
        assertEquals(1000, config.defaults().chunkSize());
    }

    @Test
    void getConfigReturnsConfigOptions() {
        apiServer.createContext("/config/options", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("GET", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200, """
                    {"defaults":{"embedding":false},"rules":[{"id":"r1","name":"Rule","conditions":[],"config":{}}]}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var config = client.getConfig();

        assertNotNull(config);
        assertEquals(false, config.defaults().embedding());
        assertEquals(1, config.rules().size());
    }

    @Test
    void getConfigDefaultsReturnsProcessingOptions() {
        apiServer.createContext("/config/options/defaults", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("GET", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200, """
                    {"chunking":true,"embedding":true,"chunk_size":1500}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var defaults = client.getConfigDefaults();

        assertNotNull(defaults);
        assertEquals(true, defaults.chunking());
        assertEquals(true, defaults.embedding());
        assertEquals(1500, defaults.chunkSize());
    }

    @Test
    void updateConfigDefaultsSendsBodyAndReturnsUpdated() {
        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/config/options/defaults", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("PUT", exchange.getRequestMethod());
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {"chunking":false}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var updated = client.updateConfigDefaults(ProcessingOptions.builder().chunking(false).build());

        assertNotNull(updated);
        assertEquals(false, updated.chunking());
        assertTrue(capturedBody.get().contains("chunking"));
    }

    @Test
    void resetConfigDefaultsSendsDelete() {
        var capturedMethod = new AtomicReference<String>();
        apiServer.createContext("/config/options/defaults", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedMethod.set(exchange.getRequestMethod());
            TestHttpServers.respondEmpty(exchange, 204);
        });
        apiServer.start();
        client = buildClient();

        client.resetConfigDefaults();

        assertEquals("DELETE", capturedMethod.get());
    }

    @Test
    void listConfigRulesReturnsRules() {
        apiServer.createContext("/config/options/rules", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("GET", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200,
                    """
                            [{"id":"r1","name":"PDF Rule","conditions":[{"field":"type","value":"pdf"}],"config":{"chunking":true}}]
                            """);
        });
        apiServer.start();
        client = buildClient();

        var rules = client.listConfigRules();

        assertEquals(1, rules.size());
        assertEquals("PDF Rule", rules.get(0).name());
    }

    @Test
    void createConfigRuleSendsBodyAndReturnsCreated() {
        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/config/options/rules", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("POST", exchange.getRequestMethod());
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 201, """
                    {"id":"new-rule","name":"Test Rule","conditions":[],"config":{}}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var rule = ConfigRule.builder().name("Test Rule").addCondition("type", "pdf").build();
        var created = client.createConfigRule(rule);

        assertNotNull(created);
        assertEquals("new-rule", created.id());
        assertTrue(capturedBody.get().contains("Test Rule"));
    }

    @Test
    void getConfigRuleReturnsRule() {
        apiServer.createContext("/config/options/rules/r1", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("GET", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200, """
                    {"id":"r1","name":"Fetched Rule","conditions":[],"config":{"embedding":true}}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var rule = client.getConfigRule("r1");

        assertEquals("Fetched Rule", rule.name());
        assertEquals(true, rule.config().embedding());
    }

    @Test
    void updateConfigRuleSendsBodyAndReturnsUpdated() {
        apiServer.createContext("/config/options/rules/r1", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("PUT", exchange.getRequestMethod());
            TestHttpServers.respondJson(exchange, 200, """
                    {"id":"r1","name":"Updated Rule","conditions":[],"config":{}}
                    """);
        });
        apiServer.start();
        client = buildClient();

        var rule = ConfigRule.builder().name("Updated Rule").build();
        var updated = client.updateConfigRule("r1", rule);

        assertEquals("Updated Rule", updated.name());
    }

    @Test
    void deleteConfigRuleSendsDelete() {
        var capturedMethod = new AtomicReference<String>();
        apiServer.createContext("/config/options/rules/r1", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            capturedMethod.set(exchange.getRequestMethod());
            TestHttpServers.respondEmpty(exchange, 204);
        });
        apiServer.start();
        client = buildClient();

        client.deleteConfigRule("r1");

        assertEquals("DELETE", capturedMethod.get());
    }

    @Test
    void testConfigRulesSendsBodyAndReturnsResponse() {
        var capturedBody = new AtomicReference<String>();
        apiServer.createContext("/config/options/rules/test", exchange -> {
            TestHttpServers.assertBearerToken(exchange);
            assertEquals("POST", exchange.getRequestMethod());
            capturedBody.set(TestHttpServers.readRequestBody(exchange));
            TestHttpServers.respondJson(exchange, 200, """
                    {
                      "matchedRule":{"id":"r1","name":"Matched","conditions":[],"config":{"chunking":true}},
                      "effectiveConfig":{"chunking":true,"chunk_size":2000}
                    }
                    """);
        });
        apiServer.start();
        client = buildClient();

        var testRequest = RuleTestRequest.builder().property("content_type", "application/pdf").build();
        var response = client.testConfigRules(testRequest);

        assertNotNull(response);
        assertEquals("Matched", response.matchedRule().name());
        assertEquals(2000, response.effectiveConfig().chunkSize());
        assertTrue(capturedBody.get().contains("application/pdf"));
    }

    // --- Error handling ---

    @Test
    void presignThrowsOnServerError() {
        apiServer.createContext("/presign", exchange -> {
            TestHttpServers.respondJson(exchange, 500, """
                    {"error":"Internal Server Error"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        assertThrows(CICSdkException.class, () -> client.presign(null));
    }

    @Test
    void getJobStatusThrowsOnServerError() {
        apiServer.createContext("/status/job-err", exchange -> {
            TestHttpServers.respondJson(exchange, 500, """
                    {"error":"Internal Server Error"}
                    """);
        });
        apiServer.start();
        client = buildClient();

        assertThrows(CICSdkException.class, () -> client.getJobStatus("job-err"));
    }
}
