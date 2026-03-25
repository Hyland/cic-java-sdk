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
package org.hyland.sdk.cic.agent.mapper;

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
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
public class AgentMapperFactory implements MapperService.MapperFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        if (AgentConfiguration.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AgentConfigurationMapper();
        } else if (AgentSummary.ListOf.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AgentSummaryMapper.ListMapper();
        } else if (AgentSummary.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AgentSummaryMapper();
        } else if (CreateAgent.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new CreateAgentMapper();
        } else if (UpdateAgent.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new UpdateAgentMapper();
        } else if (SubmitQuestionRequest.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new SubmitQuestionRequestMapper();
        } else if (IntegrationSubmitQuestionRequest.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new IntegrationSubmitQuestionRequestMapper();
        } else if (Avatar.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AvatarMapper();
        } else if (AgentAvatar.List.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AgentAvatarMapper.ListMapper();
        } else if (AgentAvatar.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new AgentAvatarMapper();
        } else if (StaticAvatar.List.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new StaticAvatarMapper.ListMapper();
        } else if (StaticAvatar.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new StaticAvatarMapper();
        } else if (LlmModel.List.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new LlmModelMapper.ListMapper();
        } else if (LlmModel.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new LlmModelMapper();
        } else if (GuardrailsResponse.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new GuardrailsResponseMapper();
        }
        return null;
    }
}
