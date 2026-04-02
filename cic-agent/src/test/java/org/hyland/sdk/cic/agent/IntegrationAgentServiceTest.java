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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.AgentSummary.ListOf;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;

/**
 * @since 1.0.0
 */
class IntegrationAgentServiceTest {

    private TestAgentHttpClient httpClient;

    private IntegrationAgentService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestAgentHttpClient();
        service = new IntegrationAgentService(httpClient);
    }

    @Test
    void testListAgents() {
        var sourceId = UUID.randomUUID().toString();
        var expected = new ListOf();
        httpClient.agentSummaries = expected;

        var result = service.listAgents(sourceId);

        assertEquals(expected, result);
        assertEquals(sourceId, httpClient.lastListSourceId);
    }

    @Test
    void testListAgentsNullSourceIdThrows() {
        assertThrows(NullPointerException.class, () -> service.listAgents(null));
    }

    @Test
    void testAgentResourceGetConfiguration() {
        var agentId = UUID.randomUUID().toString();
        httpClient.agentConfiguration = new AgentConfiguration(agentId, "Agent", "Desc", "model", null, null, null,
                List.of(), List.of(), 1, true, null, null, null, null, List.of(), null, null, null);

        var agent = service.agent(agentId);

        assertEquals(agentId, agent.id());
        assertEquals(agentId, agent.getConfiguration().id());
        assertEquals(agentId, httpClient.lastGetIntegrationAgentId);
    }

    @Test
    void testAgentResourceSubmitQuestion() {
        var agentId = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();
        var request = IntegrationSubmitQuestionRequest.builder("What is the status?", userId).build();
        var expectedQuestionId = UUID.randomUUID().toString();
        httpClient.questionResponse = new QuestionResponse(expectedQuestionId);

        var result = service.agent(agentId).submitQuestion(request);

        assertEquals(expectedQuestionId, result.questionId());
        assertEquals(agentId, httpClient.lastIntegrationQuestionAgentId);
        assertEquals(request, httpClient.lastIntegrationQuestionRequest);
    }

    @Test
    void testAgentResourceSubmitQuestionWithConsumer() {
        var agentId = UUID.randomUUID().toString();
        var userId = UUID.randomUUID().toString();
        var expectedQuestionId = UUID.randomUUID().toString();
        httpClient.questionResponse = new QuestionResponse(expectedQuestionId);

        var result = service.agent(agentId).submitQuestion(r -> r.question("Hello?").userId(userId));

        assertEquals(expectedQuestionId, result.questionId());
        assertEquals(agentId, httpClient.lastIntegrationQuestionAgentId);
        assertEquals("Hello?", httpClient.lastIntegrationQuestionRequest.question());
        assertEquals(userId, httpClient.lastIntegrationQuestionRequest.userId());
    }

    @Test
    void testAgentNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.agent(null));
    }

    private static class TestAgentHttpClient extends AgentHttpClient {

        ListOf agentSummaries;

        AgentConfiguration agentConfiguration;

        QuestionResponse questionResponse;

        String lastListSourceId;

        String lastGetIntegrationAgentId;

        String lastIntegrationQuestionAgentId;

        IntegrationSubmitQuestionRequest lastIntegrationQuestionRequest;

        public TestAgentHttpClient() {
            super(AgentHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public List<AgentSummary> listIntegrationAgents(String sourceId) {
            lastListSourceId = sourceId;
            return agentSummaries;
        }

        @Override
        public AgentConfiguration getIntegrationAgent(String agentId) {
            lastGetIntegrationAgentId = agentId;
            return agentConfiguration;
        }

        @Override
        public QuestionResponse submitIntegrationQuestion(String agentId,
                IntegrationSubmitQuestionRequest questionRequest) {
            lastIntegrationQuestionAgentId = agentId;
            lastIntegrationQuestionRequest = questionRequest;
            return questionResponse;
        }
    }
}
