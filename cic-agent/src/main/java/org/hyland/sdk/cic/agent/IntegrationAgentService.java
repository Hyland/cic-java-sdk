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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public class IntegrationAgentService {

    protected final AgentHttpClient httpClient;

    IntegrationAgentService(AgentHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns an {@link IntegrationAgentResource} handle bound to the given agent ID.
     *
     * @param agentId the agent ID
     * @return the integration agent resource handle
     * @throws NullPointerException if agentId is null
     */
    public IntegrationAgentResource agent(String agentId) {
        return new IntegrationAgentResource(httpClient, Objects.requireNonNull(agentId, "agentId cannot be null"));
    }

    /**
     * Lists agents visible to an integration for the given source.
     *
     * @param sourceId the mandatory source ID filter
     * @return a list of agent summaries
     * @throws NullPointerException if sourceId is null
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents(String sourceId) {
        Objects.requireNonNull(sourceId, "sourceId cannot be null");
        return httpClient.listIntegrationAgents(sourceId);
    }

    /**
     * A resource handle bound to a specific agent ID for integration operations.
     *
     * @since 1.0.0
     */
    public static class IntegrationAgentResource {

        private final AgentHttpClient httpClient;

        private final String agentId;

        private IntegrationAgentResource(AgentHttpClient httpClient, String agentId) {
            this.httpClient = httpClient;
            this.agentId = agentId;
        }

        /**
         * Returns the agent ID this handle is bound to.
         *
         * @return the agent ID
         */
        public String id() {
            return agentId;
        }

        /**
         * Gets the agent configuration via the integrations endpoint.
         *
         * @return the agent configuration
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration getConfiguration() {
            return httpClient.getIntegrationAgent(agentId);
        }

        /**
         * Submits a question to this agent on behalf of a user.
         *
         * @param request the integration question request
         * @return the question response containing the question ID
         * @throws CICSdkException if the request fails
         */
        public QuestionResponse submitQuestion(IntegrationSubmitQuestionRequest request) {
            return httpClient.submitIntegrationQuestion(agentId, request);
        }

        /**
         * Submits a question to this agent on behalf of a user, using a builder consumer.
         *
         * @param consumer configures the integration question request
         * @return the question response containing the question ID
         * @throws CICSdkException if the request fails
         */
        public QuestionResponse submitQuestion(Consumer<IntegrationSubmitQuestionRequest.Builder> consumer) {
            var builder = IntegrationSubmitQuestionRequest.builder();
            consumer.accept(builder);
            return httpClient.submitIntegrationQuestion(agentId, builder.build());
        }
    }
}
