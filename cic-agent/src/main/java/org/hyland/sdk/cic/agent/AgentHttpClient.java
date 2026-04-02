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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.DELETE;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.PUT;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.Avatar;
import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.agent.object.GuardrailGroup;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.agent.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.UpdateAgent;
import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;

/**
 * HTTP client for interacting with the CIC Agent API.
 *
 * @since 1.0.0
 */
public class AgentHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String AGENTS_PATH = "/agents";

    private static final String MODELS_PATH = "/models";

    private static final String GUARDRAILS_PATH = "/guardrails";

    private static final String INTEGRATIONS_AGENTS_PATH = "/integrations/agents";

    protected AgentHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from() {
        // TODO turn this to production
        return from("https://discovery.dev.experience.hyland.com/agent");
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    /**
     * Lists agents, optionally filtered by source ID.
     *
     * @param sourceId optional source ID filter (may be null)
     * @param includePresignedUrls whether to include pre-signed avatar URLs
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listAgents(String sourceId, boolean includePresignedUrls) {
        var requestBuilder = this.requestBuilder(GET, AGENTS_PATH);
        if (sourceId != null) {
            requestBuilder.queryParameter("sourceId", sourceId);
        }
        requestBuilder.queryParameter("includePresignedUrls", String.valueOf(includePresignedUrls));
        return sendThenMapAs(requestBuilder.build(), AgentSummary.ListOf.class);
    }

    /**
     * Creates a new agent.
     *
     * @param agent the agent creation request
     * @return the created agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration createAgent(CreateAgent agent) {
        var request = this.requestBuilder(POST, AGENTS_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(agent))
                          .build();
        return sendThenMapAs(request, AgentConfiguration.class);
    }

    /**
     * Gets an agent by ID.
     *
     * @param agentId the agent ID
     * @param includePresignedUrl whether to include pre-signed avatar URL
     * @return the agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration getAgent(String agentId, boolean includePresignedUrl) {
        var request = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId)
                          .queryParameter("includePresignedUrl", String.valueOf(includePresignedUrl))
                          .build();
        return sendThenMapAs(request, AgentConfiguration.class);
    }

    /**
     * Updates an existing agent.
     *
     * @param agentId the agent ID
     * @param agent the agent update request
     * @return the updated agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration updateAgent(String agentId, UpdateAgent agent) {
        var request = this.requestBuilder(PUT, AGENTS_PATH + "/" + agentId)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(agent))
                          .build();
        return sendThenMapAs(request, AgentConfiguration.class);
    }

    /**
     * Deletes an agent.
     *
     * @param agentId the agent ID
     * @throws CICSdkException if the request fails or returns a non-204 status code
     */
    public void deleteAgent(String agentId) {
        var request = this.requestBuilder(DELETE, AGENTS_PATH + "/" + agentId).build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete agent, HTTP response returned with status code: " + response.statusCode());
        }
    }

    /**
     * Gets a specific version of an agent.
     *
     * @param agentId the agent ID
     * @param version the version number
     * @param includePresignedUrl whether to include pre-signed avatar URL
     * @return the agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration getAgentVersion(String agentId, int version, boolean includePresignedUrl) {
        var request = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/versions/" + version)
                          .queryParameter("includePresignedUrl", String.valueOf(includePresignedUrl))
                          .build();
        return sendThenMapAs(request, AgentConfiguration.class);
    }

    /**
     * Gets the avatar for an agent.
     *
     * @param agentId the agent ID
     * @return the avatar with pre-signed URL
     * @throws CICSdkException if the request fails
     */
    public Avatar getAvatar(String agentId) {
        var request = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/avatar").build();
        return sendThenMapAs(request, Avatar.class);
    }

    /**
     * Gets avatars for multiple agents in batch.
     *
     * @param agentIds the list of agent IDs
     * @return a map of agent ID to avatar
     * @throws CICSdkException if the request fails
     */
    public Map<String, Avatar> getAvatarsBatch(List<String> agentIds) {
        Objects.requireNonNull(agentIds, "agentIds cannot be null");
        var body = CICObject.create();
        var array = CICArray.create();
        agentIds.forEach(array::addString);
        body.putArray("agentIds", array);
        var request = this.requestBuilder(POST, AGENTS_PATH + "/avatars/batch")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(body))
                          .build();
        return sendThenMapAs(request, Avatar.BatchMap.class);
    }

    /**
     * Gets the list of static avatars.
     *
     * @return the list of static avatars
     * @throws CICSdkException if the request fails
     */
    public List<StaticAvatar> getStaticAvatars() {
        var request = this.requestBuilder(GET, AGENTS_PATH + "/avatars/static").build();
        return sendThenMapAs(request, StaticAvatar.ListOf.class);
    }

    /**
     * Lists available LLM models.
     *
     * @return the list of LLM models
     * @throws CICSdkException if the request fails
     */
    public List<LlmModel> listModels() {
        var request = this.requestBuilder(GET, MODELS_PATH).build();
        return sendThenMapAs(request, LlmModel.ListOf.class);
    }

    /**
     * Lists available guardrails.
     *
     * @return a list of guardrail groups
     * @throws CICSdkException if the request fails
     */
    public List<GuardrailGroup> listGuardrails() {
        var request = this.requestBuilder(GET, GUARDRAILS_PATH).build();
        return sendThenMapAs(request, GuardrailGroup.ListOf.class);
    }

    /**
     * Submits a question to an agent.
     *
     * @param agentId the agent ID
     * @param questionRequest the question request
     * @return the question response containing the question ID
     * @throws CICSdkException if the request fails
     */
    public QuestionResponse submitQuestion(String agentId, SubmitQuestionRequest questionRequest) {
        var request = this.requestBuilder(POST, AGENTS_PATH + "/" + agentId + "/questions")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(questionRequest))
                          .build();
        return sendThenMapAs(request, QuestionResponse.class);
    }

    /**
     * Gets an agent via the integrations endpoint.
     *
     * @param agentId the agent ID
     * @return the agent configuration
     * @throws CICSdkException if the request fails
     */
    public AgentConfiguration getIntegrationAgent(String agentId) {
        var request = this.requestBuilder(GET, INTEGRATIONS_AGENTS_PATH + "/" + agentId).build();
        return sendThenMapAs(request, AgentConfiguration.class);
    }

    /**
     * Lists agents via the integrations endpoint.
     *
     * @param sourceId the mandatory source ID filter
     * @return a list of agent summaries
     * @throws CICSdkException if the request fails
     */
    public List<AgentSummary> listIntegrationAgents(String sourceId) {
        Objects.requireNonNull(sourceId, "sourceId cannot be null");
        var request = this.requestBuilder(GET, INTEGRATIONS_AGENTS_PATH).queryParameter("sourceId", sourceId).build();
        return sendThenMapAs(request, AgentSummary.ListOf.class);
    }

    /**
     * Submits a question to an agent via the integrations endpoint.
     *
     * @param agentId the agent ID
     * @param questionRequest the integration question request
     * @return the question response containing the question ID
     * @throws CICSdkException if the request fails
     */
    public QuestionResponse submitIntegrationQuestion(String agentId,
            IntegrationSubmitQuestionRequest questionRequest) {
        var request = this.requestBuilder(POST, INTEGRATIONS_AGENTS_PATH + "/" + agentId + "/questions")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(questionRequest))
                          .build();
        return sendThenMapAs(request, QuestionResponse.class);
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, AgentHttpClient> {

        /**
         * Creates a builder for AgentHttpClient.
         *
         * @param baseUrl the base URL of the CIC Agent API
         * @param authenticationBuilder the authentication builder
         */
        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        /**
         * Sets the hxp-environment header.
         *
         * @param environment the environment value
         * @return this builder
         */
        public Builder hxpEnvironment(String environment) {
            return header("hxp-environment", environment);
        }

        /**
         * Sets the hxp-app header.
         *
         * @param app the app value
         * @return this builder
         */
        public Builder hxpApp(String app) {
            return header("hxp-app", app);
        }

        @Override
        public AgentHttpClient build() {
            return new AgentHttpClient(this);
        }
    }

}
