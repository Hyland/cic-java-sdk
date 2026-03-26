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

import org.hyland.sdk.cic.agent.object.AgentAvatar;
import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.Avatar;
import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.agent.object.GuardrailsResponse;
import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.agent.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.UpdateAgent;
import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public class AgentService {

    protected final AgentHttpClient httpClient;

    public AgentService(AgentHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns an {@link AgentResource} handle bound to the given agent ID.
     *
     * @param agentId the agent ID
     * @return the agent resource handle
     * @throws NullPointerException if agentId is null
     */
    public AgentResource agent(String agentId) {
        return new AgentResource(httpClient, Objects.requireNonNull(agentId, "agentId cannot be null"));
    }

    /**
     * Lists all agents.
     *
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents() {
        return httpClient.listAgents(null, false);
    }

    /**
     * Lists agents filtered by source ID.
     *
     * @param sourceId the source ID filter
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents(String sourceId) {
        return httpClient.listAgents(sourceId, false);
    }

    /**
     * Lists all agents, optionally including pre-signed avatar URLs.
     *
     * @param includePresignedUrls whether to include pre-signed avatar URLs
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents(boolean includePresignedUrls) {
        return httpClient.listAgents(null, includePresignedUrls);
    }

    /**
     * Lists agents filtered by source ID, optionally including pre-signed avatar URLs.
     *
     * @param sourceId the source ID filter
     * @param includePresignedUrls whether to include pre-signed avatar URLs
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents(String sourceId, boolean includePresignedUrls) {
        return httpClient.listAgents(sourceId, includePresignedUrls);
    }

    /**
     * Creates a new agent.
     *
     * @param agent the agent creation request
     * @return the created agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration createAgent(CreateAgent agent) {
        return httpClient.createAgent(agent);
    }

    /**
     * Creates a new agent using a builder consumer.
     *
     * @param consumer configures the agent creation request
     * @return the created agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration createAgent(Consumer<CreateAgent.Builder> consumer) {
        var builder = CreateAgent.builder();
        consumer.accept(builder);
        return httpClient.createAgent(builder.build());
    }

    /**
     * Gets an agent by ID.
     *
     * @param agentId the agent ID
     * @return the agent configuration
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration getAgent(String agentId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        return httpClient.getAgent(agentId, false);
    }

    /**
     * Updates an existing agent.
     *
     * @param agentId the agent ID
     * @param agent the agent update request
     * @return the updated agent configuration
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration updateAgent(String agentId, UpdateAgent agent) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        return httpClient.updateAgent(agentId, agent);
    }

    /**
     * Updates an existing agent using a builder consumer.
     *
     * @param agentId the agent ID
     * @param consumer configures the agent update request
     * @return the updated agent configuration
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration updateAgent(String agentId, Consumer<UpdateAgent.Builder> consumer) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var builder = UpdateAgent.builder();
        consumer.accept(builder);
        return httpClient.updateAgent(agentId, builder.build());
    }

    /**
     * Deletes an agent.
     *
     * @param agentId the agent ID
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public void deleteAgent(String agentId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        httpClient.deleteAgent(agentId);
    }

    /**
     * Gets a specific version of an agent.
     *
     * @param agentId the agent ID
     * @param version the version number
     * @return the agent configuration for the given version
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration getAgentVersion(String agentId, int version) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        return httpClient.getAgentVersion(agentId, version, false);
    }

    /**
     * Gets the avatar for an agent.
     *
     * @param agentId the agent ID
     * @return the avatar with pre-signed URL
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public Avatar getAvatar(String agentId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        return httpClient.getAvatar(agentId);
    }

    /**
     * Gets avatars for multiple agents in batch.
     *
     * @param agentIds the list of agent IDs
     * @return the list of agent avatars
     * @throws CICSdkException if the request fails
     */
    public List<AgentAvatar> getAvatarsBatch(List<String> agentIds) {
        return httpClient.getAvatarsBatch(agentIds);
    }

    /**
     * Gets the list of static avatars.
     *
     * @return the list of static avatars
     * @throws CICSdkException if the request fails
     */
    public List<StaticAvatar> getStaticAvatars() {
        return httpClient.getStaticAvatars();
    }

    /**
     * Lists available LLM models.
     *
     * @return the list of LLM models
     * @throws CICSdkException if the request fails
     */
    public List<LlmModel> listModels() {
        return httpClient.listModels();
    }

    /**
     * Lists available guardrails.
     *
     * @return the guardrails response
     * @throws CICSdkException if the request fails
     */
    public GuardrailsResponse listGuardrails() {
        return httpClient.listGuardrails();
    }

    /**
     * Submits a question to an agent.
     *
     * @param agentId the agent ID
     * @param request the question request
     * @return the question response containing the question ID
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public QuestionResponse submitQuestion(String agentId, SubmitQuestionRequest request) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        return httpClient.submitQuestion(agentId, request);
    }

    /**
     * Submits a question to an agent using a builder consumer.
     *
     * @param agentId the agent ID
     * @param consumer configures the question request
     * @return the question response containing the question ID
     * @throws NullPointerException if agentId is null
     * @throws CICSdkException if the request fails
     */
    public QuestionResponse submitQuestion(String agentId, Consumer<SubmitQuestionRequest.Builder> consumer) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var builder = SubmitQuestionRequest.builder();
        consumer.accept(builder);
        return httpClient.submitQuestion(agentId, builder.build());
    }

    /**
     * A resource handle bound to a specific agent ID. Provides agent-scoped operations without repeatedly passing the
     * agent ID.
     *
     * @since 1.0.0
     */
    public static class AgentResource {

        private final AgentHttpClient httpClient;

        private final String agentId;

        private AgentResource(AgentHttpClient httpClient, String agentId) {
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
         * Gets the agent configuration.
         *
         * @return the agent configuration
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration getConfiguration() {
            return httpClient.getAgent(agentId, false);
        }

        /**
         * Gets the agent configuration, optionally including a pre-signed avatar URL.
         *
         * @param includePresignedUrl whether to include the pre-signed avatar URL
         * @return the agent configuration
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration getConfiguration(boolean includePresignedUrl) {
            return httpClient.getAgent(agentId, includePresignedUrl);
        }

        /**
         * Updates this agent.
         *
         * @param agent the agent update request
         * @return the updated agent configuration
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration update(UpdateAgent agent) {
            return httpClient.updateAgent(agentId, agent);
        }

        /**
         * Updates this agent using a builder consumer.
         *
         * @param consumer configures the agent update request
         * @return the updated agent configuration
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration update(Consumer<UpdateAgent.Builder> consumer) {
            var builder = UpdateAgent.builder();
            consumer.accept(builder);
            return httpClient.updateAgent(agentId, builder.build());
        }

        /**
         * Deletes this agent.
         *
         * @throws CICSdkException if the request fails
         */
        public void delete() {
            httpClient.deleteAgent(agentId);
        }

        /**
         * Gets a specific version of this agent.
         *
         * @param version the version number
         * @return the agent configuration for the given version
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration getVersion(int version) {
            return httpClient.getAgentVersion(agentId, version, false);
        }

        /**
         * Gets a specific version of this agent, optionally including a pre-signed avatar URL.
         *
         * @param version the version number
         * @param includePresignedUrl whether to include the pre-signed avatar URL
         * @return the agent configuration for the given version
         * @throws CICSdkException if the request fails
         */
        public AgentConfiguration getVersion(int version, boolean includePresignedUrl) {
            return httpClient.getAgentVersion(agentId, version, includePresignedUrl);
        }

        /**
         * Gets the avatar for this agent.
         *
         * @return the avatar with pre-signed URL
         * @throws CICSdkException if the request fails
         */
        public Avatar getAvatar() {
            return httpClient.getAvatar(agentId);
        }

        /**
         * Submits a question to this agent.
         *
         * @param request the question request
         * @return the question response containing the question ID
         * @throws CICSdkException if the request fails
         */
        public QuestionResponse submitQuestion(SubmitQuestionRequest request) {
            return httpClient.submitQuestion(agentId, request);
        }

        /**
         * Submits a question to this agent using a builder consumer.
         *
         * @param consumer configures the question request
         * @return the question response containing the question ID
         * @throws CICSdkException if the request fails
         */
        public QuestionResponse submitQuestion(Consumer<SubmitQuestionRequest.Builder> consumer) {
            var builder = SubmitQuestionRequest.builder();
            consumer.accept(builder);
            return httpClient.submitQuestion(agentId, builder.build());
        }
    }
}
