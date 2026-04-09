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

import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.agent.object.AgentAvatar;
import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.Avatar;
import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.agent.object.GuardrailsResponse;
import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.agent.object.QuestionResponse;
import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.agent.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.agent.object.UpdateAgent;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
public class AgentMapperFactory implements MapperService.MapperFactory {

    // Stateless mappers are shared — order matters for types in the same hierarchy
    private static final List<Map.Entry<Class<?>, CICMapper<?>>> MAPPERS = List.of(
            Map.entry(AgentConfiguration.class, new AgentConfigurationMapper()),
            Map.entry(AgentSummary.ListOf.class, new AgentSummaryMapper.ListMapper()),
            Map.entry(AgentSummary.class, new AgentSummaryMapper()),
            Map.entry(CreateAgent.class, new CreateAgentMapper()),
            Map.entry(UpdateAgent.class, new UpdateAgentMapper()),
            Map.entry(SubmitQuestionRequest.class, new SubmitQuestionRequestMapper()),
            Map.entry(IntegrationSubmitQuestionRequest.class, new IntegrationSubmitQuestionRequestMapper()),
            Map.entry(Avatar.class, new AvatarMapper()),
            Map.entry(AgentAvatar.List.class, new AgentAvatarMapper.ListMapper()),
            Map.entry(AgentAvatar.class, new AgentAvatarMapper()),
            Map.entry(StaticAvatar.List.class, new StaticAvatarMapper.ListMapper()),
            Map.entry(StaticAvatar.class, new StaticAvatarMapper()),
            Map.entry(LlmModel.List.class, new LlmModelMapper.ListMapper()),
            Map.entry(LlmModel.class, new LlmModelMapper()),
            Map.entry(GuardrailsResponse.class, new GuardrailsResponseMapper()),
            Map.entry(QuestionResponse.class, new QuestionResponseMapper()));

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        for (var entry : MAPPERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return (CICMapper<T>) entry.getValue();
            }
        }
        return null;
    }
}
