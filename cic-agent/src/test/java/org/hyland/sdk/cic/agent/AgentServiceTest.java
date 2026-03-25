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
 *     Damian Ujma <damian.ujma@hyland.com>
 */
package org.hyland.sdk.cic.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.AgentAvatar;
import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.Avatar;
import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.agent.object.GuardrailsResponse;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.agent.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.UpdateAgent;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;

/**
 * @since 1.0.0
 */
class AgentServiceTest {

    private TestAgentHttpClient httpClient;

    private AgentService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestAgentHttpClient();
        service = new AgentService(httpClient);
    }

    @Test
    void testListAgents() {
        var expected = new AgentSummary.ListOf();
        httpClient.agentSummaries = expected;

        var result = service.listAgents();

        assertEquals(expected, result);
        assertNull(httpClient.lastListSourceId);
    }

    @Test
    void testListAgentsWithSourceId() {
        var sourceId = UUID.randomUUID();
        httpClient.agentSummaries = new AgentSummary.ListOf();

        service.listAgents(sourceId);

        assertEquals(sourceId, httpClient.lastListSourceId);
    }

    @Test
    void testCreateAgent() {
        var createAgent = CreateAgent.builder("Test Agent", "A test agent", "bedrock-amazon-nova-micro").build();
        var expectedId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(expectedId, "Test Agent", "A test agent",
                "bedrock-amazon-nova-micro", null, null, null, List.of(), List.of(), 1, true, null, null, null, null,
                List.of(), null, null, null);

        var result = service.createAgent(createAgent);

        assertEquals(expectedId, result.id());
        assertEquals("Test Agent", result.name());
        assertEquals(createAgent, httpClient.lastCreateAgent);
    }

    @Test
    void testGetAgent() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var result = service.getAgent(agentId);

        assertEquals(agentId, result.id());
        assertEquals(agentId, httpClient.lastGetAgentId);
    }

    @Test
    void testUpdateAgent() {
        var agentId = UUID.randomUUID();
        var updateAgent = UpdateAgent.builder("Updated", "Updated desc", "model").build();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Updated", "Updated desc", "model", null, null,
                null, List.of(), List.of(), 2, true, null, null, null, null, List.of(), null, null, null);

        var result = service.updateAgent(agentId, updateAgent);

        assertEquals("Updated", result.name());
        assertEquals(agentId, httpClient.lastUpdateAgentId);
        assertEquals(updateAgent, httpClient.lastUpdateAgent);
    }

    @Test
    void testDeleteAgent() {
        var agentId = UUID.randomUUID();

        service.deleteAgent(agentId);

        assertEquals(agentId, httpClient.lastDeleteAgentId);
    }

    @Test
    void testSubmitQuestion() {
        var agentId = UUID.randomUUID();
        var questionRequest = SubmitQuestionRequest.builder("What is the status?").build();

        service.submitQuestion(agentId, questionRequest);

        assertEquals(agentId, httpClient.lastQuestionAgentId);
        assertEquals(questionRequest, httpClient.lastQuestionRequest);
    }

    @Test
    void testListModels() {
        var expected = new LlmModel.List();
        expected.add(new LlmModel("Nova Micro", "amazon.nova-micro-v1:0", "Active", null, null));
        httpClient.llmModels = expected;

        var result = service.listModels();

        assertEquals(1, result.size());
        assertEquals("Nova Micro", result.get(0).displayName());
    }

    @Test
    void testListGuardrails() {
        httpClient.guardrailsResponse = new GuardrailsResponse(List.of());

        var result = service.listGuardrails();

        assertTrue(result.guardrailGroups().isEmpty());
    }

    @Test
    void testGetAvatar() {
        var agentId = UUID.randomUUID();
        httpClient.avatar = new Avatar("https://example.com/avatar.png");

        var result = service.getAvatar(agentId);

        assertEquals("https://example.com/avatar.png", result.preSignedUrl());
    }

    @Test
    void testGetStaticAvatars() {
        var expected = new StaticAvatar.List();
        expected.add(new StaticAvatar("avatar.png", "https://example.com/avatar.png"));
        httpClient.staticAvatars = expected;

        var result = service.getStaticAvatars();

        assertEquals(1, result.size());
        assertEquals("avatar.png", result.get(0).fileName());
    }

    @Test
    void testGetIntegrationAgent() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var result = service.getIntegrationAgent(agentId);

        assertEquals(agentId, result.id());
    }

    @Test
    void testSubmitIntegrationQuestion() {
        var agentId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var questionRequest = IntegrationSubmitQuestionRequest.builder("What is the status?", userId).build();

        service.submitIntegrationQuestion(agentId, questionRequest);

        assertEquals(agentId, httpClient.lastIntegrationQuestionAgentId);
        assertEquals(questionRequest, httpClient.lastIntegrationQuestionRequest);
    }

    // -- Consumer-based API tests --

    @Test
    void testCreateAgentWithConsumer() {
        var expectedId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(expectedId, "Agent", "Desc", "bedrock-amazon-nova-micro",
                null, null, null, List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var result = service.createAgent(
                r -> r.name("Agent").description("Desc").modelName("bedrock-amazon-nova-micro"));

        assertEquals(expectedId, result.id());
        assertEquals("Agent", httpClient.lastCreateAgent.name());
        assertEquals("Desc", httpClient.lastCreateAgent.description());
        assertEquals("bedrock-amazon-nova-micro", httpClient.lastCreateAgent.modelName());
    }

    @Test
    void testUpdateAgentWithConsumer() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Updated", "Updated desc", "model", null, null,
                null, List.of(), List.of(), 2, true, null, null, null, null, List.of(), null, null, null);

        var result = service.updateAgent(agentId,
                r -> r.name("Updated").description("Updated desc").modelName("model"));

        assertEquals("Updated", result.name());
        assertEquals(agentId, httpClient.lastUpdateAgentId);
        assertEquals("Updated", httpClient.lastUpdateAgent.name());
    }

    @Test
    void testSubmitQuestionWithConsumer() {
        var agentId = UUID.randomUUID();

        service.submitQuestion(agentId, r -> r.question("What is the status?"));

        assertEquals(agentId, httpClient.lastQuestionAgentId);
        assertEquals("What is the status?", httpClient.lastQuestionRequest.question());
    }

    @Test
    void testSubmitIntegrationQuestionWithConsumer() {
        var agentId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        service.submitIntegrationQuestion(agentId, r -> r.question("What is the status?").userId(userId));

        assertEquals(agentId, httpClient.lastIntegrationQuestionAgentId);
        assertEquals("What is the status?", httpClient.lastIntegrationQuestionRequest.question());
        assertEquals(userId, httpClient.lastIntegrationQuestionRequest.userId());
    }

    // -- AgentResource tests --

    @Test
    void testAgentResourceGet() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var agent = service.agent(agentId);

        assertEquals(agentId, agent.id());
        assertEquals(agentId, agent.get().id());
    }

    @Test
    void testAgentResourceGetWithPresignedUrl() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var result = service.agent(agentId).get(true);

        assertEquals(agentId, result.id());
        assertTrue(httpClient.lastIncludePresignedUrl);
    }

    @Test
    void testAgentResourceUpdate() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Updated", "Desc", "model", null, null, null,
                List.of(), List.of(), 2, true, null, null, null, null, List.of(), null, null, null);

        var result = service.agent(agentId).update(r -> r.name("Updated").description("Desc").modelName("model"));

        assertEquals("Updated", result.name());
        assertEquals(agentId, httpClient.lastUpdateAgentId);
    }

    @Test
    void testAgentResourceDelete() {
        var agentId = UUID.randomUUID();

        service.agent(agentId).delete();

        assertEquals(agentId, httpClient.lastDeleteAgentId);
    }

    @Test
    void testAgentResourceVersion() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 3, true, null, null, null, null, List.of(), null, null, null);

        var result = service.agent(agentId).version(3);

        assertEquals(3, result.version());
        assertEquals(agentId, httpClient.lastVersionAgentId);
        assertEquals(3, httpClient.lastVersion);
    }

    @Test
    void testAgentResourceVersionWithPresignedUrl() {
        var agentId = UUID.randomUUID();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 3, true, null, null, null, null, List.of(), null, null, null);

        var result = service.agent(agentId).version(3, true);

        assertEquals(3, result.version());
        assertTrue(httpClient.lastIncludePresignedUrl);
    }

    @Test
    void testAgentResourceAvatar() {
        var agentId = UUID.randomUUID();
        httpClient.avatar = new Avatar("https://example.com/avatar.png");

        var result = service.agent(agentId).avatar();

        assertEquals("https://example.com/avatar.png", result.preSignedUrl());
    }

    @Test
    void testAgentResourceSubmitQuestion() {
        var agentId = UUID.randomUUID();

        service.agent(agentId).submitQuestion(r -> r.question("Hello?"));

        assertEquals(agentId, httpClient.lastQuestionAgentId);
        assertEquals("Hello?", httpClient.lastQuestionRequest.question());
    }

    @Test
    void testAgentResourceSubmitIntegrationQuestion() {
        var agentId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        service.agent(agentId).submitIntegrationQuestion(r -> r.question("Hello?").userId(userId));

        assertEquals(agentId, httpClient.lastIntegrationQuestionAgentId);
        assertEquals("Hello?", httpClient.lastIntegrationQuestionRequest.question());
    }

    @Test
    void testAgentResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.agent(null));
    }

    private static class TestAgentHttpClient extends AgentHttpClient {

        AgentSummary.ListOf agentSummaries;

        AgentConfiguration agentConfiguration;

        LlmModel.List llmModels;

        GuardrailsResponse guardrailsResponse;

        Avatar avatar;

        StaticAvatar.List staticAvatars;

        UUID lastListSourceId;

        CreateAgent lastCreateAgent;

        UUID lastGetAgentId;

        UUID lastUpdateAgentId;

        UpdateAgent lastUpdateAgent;

        UUID lastDeleteAgentId;

        UUID lastQuestionAgentId;

        SubmitQuestionRequest lastQuestionRequest;

        UUID lastIntegrationQuestionAgentId;

        IntegrationSubmitQuestionRequest lastIntegrationQuestionRequest;

        UUID lastVersionAgentId;

        int lastVersion;

        boolean lastIncludePresignedUrl;

        public TestAgentHttpClient() {
            super(AgentHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public List<AgentSummary> listAgents(UUID sourceId, boolean includePresignedUrls) {
            lastListSourceId = sourceId;
            return agentSummaries;
        }

        @Override
        public AgentConfiguration createAgent(CreateAgent agent) {
            lastCreateAgent = agent;
            return agentConfiguration;
        }

        @Override
        public AgentConfiguration getAgent(UUID agentId, boolean includePresignedUrl) {
            lastGetAgentId = agentId;
            lastIncludePresignedUrl = includePresignedUrl;
            return agentConfiguration;
        }

        @Override
        public AgentConfiguration getAgentVersion(UUID agentId, int version, boolean includePresignedUrl) {
            lastVersionAgentId = agentId;
            lastVersion = version;
            lastIncludePresignedUrl = includePresignedUrl;
            return agentConfiguration;
        }

        @Override
        public AgentConfiguration updateAgent(UUID agentId, UpdateAgent agent) {
            lastUpdateAgentId = agentId;
            lastUpdateAgent = agent;
            return agentConfiguration;
        }

        @Override
        public void deleteAgent(UUID agentId) {
            lastDeleteAgentId = agentId;
        }

        @Override
        public void submitQuestion(UUID agentId, SubmitQuestionRequest questionRequest) {
            lastQuestionAgentId = agentId;
            lastQuestionRequest = questionRequest;
        }

        @Override
        public List<LlmModel> listModels() {
            return llmModels;
        }

        @Override
        public GuardrailsResponse listGuardrails() {
            return guardrailsResponse;
        }

        @Override
        public Avatar getAvatar(UUID agentId) {
            return avatar;
        }

        @Override
        public List<AgentAvatar> getAvatarsBatch(List<UUID> agentIds) {
            return new ArrayList<>();
        }

        @Override
        public List<StaticAvatar> getStaticAvatars() {
            return staticAvatars;
        }

        @Override
        public AgentConfiguration getIntegrationAgent(UUID agentId) {
            return agentConfiguration;
        }

        @Override
        public List<AgentSummary> listIntegrationAgents(UUID sourceId) {
            return agentSummaries;
        }

        @Override
        public void submitIntegrationQuestion(UUID agentId, IntegrationSubmitQuestionRequest questionRequest) {
            lastIntegrationQuestionAgentId = agentId;
            lastIntegrationQuestionRequest = questionRequest;
        }
    }
}
