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
import java.util.UUID;
import java.util.function.Consumer;

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

/**
 * High-level service for Agent API operations.
 * <p>
 * Supports both pre-built request objects
 *
 * <pre>{@code
 * // Consumer-based (fluent)
 * service.createAgent(r -> r.name("Agent").description("Desc").modelName("nova-micro"));
 *
 * // Resource-centric
 * var agent = service.agent(agentId);
 * agent.update(r -> r.name("New Name").description("Desc").modelName("nova-micro"));
 * agent.submitQuestion(r -> r.question("What is the status?"));
 * agent.delete();
 * }</pre>
 *
 * @since 1.0.0
 */
public class AgentService {

    protected final AgentHttpClient httpClient;

    public AgentService(AgentHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // -- Resource Handle --

    /**
     * Returns an {@link AgentResource} handle bound to the given agent ID.
     *
     * @param agentId the agent ID
     * @return the agent resource handle
     */
    public AgentResource agent(UUID agentId) {
        return new AgentResource(Objects.requireNonNull(agentId, "agentId cannot be null"));
    }

    // -- Agent CRUD --

    public List<AgentSummary> listAgents() {
        return httpClient.listAgents(null, false);
    }

    public List<AgentSummary> listAgents(UUID sourceId) {
        return httpClient.listAgents(sourceId, false);
    }

    public AgentConfiguration createAgent(CreateAgent agent) {
        return httpClient.createAgent(agent);
    }

    public AgentConfiguration createAgent(Consumer<CreateAgent.Builder> consumer) {
        var builder = CreateAgent.builder();
        consumer.accept(builder);
        return httpClient.createAgent(builder.build());
    }

    public AgentConfiguration getAgent(UUID agentId) {
        return httpClient.getAgent(agentId, false);
    }

    public AgentConfiguration updateAgent(UUID agentId, UpdateAgent agent) {
        return httpClient.updateAgent(agentId, agent);
    }

    public AgentConfiguration updateAgent(UUID agentId, Consumer<UpdateAgent.Builder> consumer) {
        var builder = UpdateAgent.builder();
        consumer.accept(builder);
        return httpClient.updateAgent(agentId, builder.build());
    }

    public void deleteAgent(UUID agentId) {
        httpClient.deleteAgent(agentId);
    }

    public AgentConfiguration getAgentVersion(UUID agentId, int version) {
        return httpClient.getAgentVersion(agentId, version, false);
    }

    // -- Avatars --

    public Avatar getAvatar(UUID agentId) {
        return httpClient.getAvatar(agentId);
    }

    public List<AgentAvatar> getAvatarsBatch(List<UUID> agentIds) {
        return httpClient.getAvatarsBatch(agentIds);
    }

    public List<StaticAvatar> getStaticAvatars() {
        return httpClient.getStaticAvatars();
    }

    // -- Models & Guardrails --

    public List<LlmModel> listModels() {
        return httpClient.listModels();
    }

    public GuardrailsResponse listGuardrails() {
        return httpClient.listGuardrails();
    }

    // -- Questions --

    public void submitQuestion(UUID agentId, SubmitQuestionRequest request) {
        httpClient.submitQuestion(agentId, request);
    }

    public void submitQuestion(UUID agentId, Consumer<SubmitQuestionRequest.Builder> consumer) {
        var builder = SubmitQuestionRequest.builder();
        consumer.accept(builder);
        httpClient.submitQuestion(agentId, builder.build());
    }

    // -- Integrations --

    public AgentConfiguration getIntegrationAgent(UUID agentId) {
        return httpClient.getIntegrationAgent(agentId);
    }

    public List<AgentSummary> listIntegrationAgents(UUID sourceId) {
        return httpClient.listIntegrationAgents(sourceId);
    }

    public void submitIntegrationQuestion(UUID agentId, IntegrationSubmitQuestionRequest request) {
        httpClient.submitIntegrationQuestion(agentId, request);
    }

    public void submitIntegrationQuestion(UUID agentId, Consumer<IntegrationSubmitQuestionRequest.Builder> consumer) {
        var builder = IntegrationSubmitQuestionRequest.builder();
        consumer.accept(builder);
        httpClient.submitIntegrationQuestion(agentId, builder.build());
    }

    /**
     * A resource handle bound to a specific agent ID. Provides agent-scoped operations without repeatedly passing the
     * agent ID.
     *
     * @since 1.0.0
     */
    public class AgentResource {

        private final UUID agentId;

        private AgentResource(UUID agentId) {
            this.agentId = agentId;
        }

        public UUID id() {
            return agentId;
        }

        public AgentConfiguration get() {
            return httpClient.getAgent(agentId, false);
        }

        public AgentConfiguration get(boolean includePresignedUrl) {
            return httpClient.getAgent(agentId, includePresignedUrl);
        }

        public AgentConfiguration update(UpdateAgent agent) {
            return httpClient.updateAgent(agentId, agent);
        }

        public AgentConfiguration update(Consumer<UpdateAgent.Builder> consumer) {
            var builder = UpdateAgent.builder();
            consumer.accept(builder);
            return httpClient.updateAgent(agentId, builder.build());
        }

        public void delete() {
            httpClient.deleteAgent(agentId);
        }

        public AgentConfiguration version(int version) {
            return httpClient.getAgentVersion(agentId, version, false);
        }

        public AgentConfiguration version(int version, boolean includePresignedUrl) {
            return httpClient.getAgentVersion(agentId, version, includePresignedUrl);
        }

        public Avatar avatar() {
            return httpClient.getAvatar(agentId);
        }

        public void submitQuestion(SubmitQuestionRequest request) {
            httpClient.submitQuestion(agentId, request);
        }

        public void submitQuestion(Consumer<SubmitQuestionRequest.Builder> consumer) {
            var builder = SubmitQuestionRequest.builder();
            consumer.accept(builder);
            httpClient.submitQuestion(agentId, builder.build());
        }

        public void submitIntegrationQuestion(IntegrationSubmitQuestionRequest request) {
            httpClient.submitIntegrationQuestion(agentId, request);
        }

        public void submitIntegrationQuestion(Consumer<IntegrationSubmitQuestionRequest.Builder> consumer) {
            var builder = IntegrationSubmitQuestionRequest.builder();
            consumer.accept(builder);
            httpClient.submitIntegrationQuestion(agentId, builder.build());
        }
    }
}
