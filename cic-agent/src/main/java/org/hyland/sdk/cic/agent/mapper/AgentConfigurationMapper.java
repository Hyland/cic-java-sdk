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

import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readAccessRights;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readFilterExpressionOrNull;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readGuardrails;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readRagParameters;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readStringList;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readStringOrNull;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AgentConfigurationMapper implements CICMapper<AgentConfiguration> {

    @Override
    public AgentConfiguration fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new AgentConfiguration(obj.getStringOrThrow("id"), obj.getStringOrThrow("name"),
                obj.getStringOrThrow("description"), obj.getStringOrThrow("modelName"),
                obj.getString("avatarUrl", null), obj.getString("avatarPresignedUrl", null),
                obj.getString("instructions", null), readStringList(obj, "sourceIds"), readAccessRights(obj),
                obj.getInt("version", 0), obj.getBoolean("latest", false),
                readFilterExpressionOrNull(obj, "staticFilterExpression"),
                readFilterExpressionOrNull(obj, "dynamicFilterTemplate"), readStringOrNull(obj, "agentPlatformAgentId"),
                readStringOrNull(obj, "agentPlatformAgentVersionId"), readGuardrails(obj), readRagParameters(obj),
                obj.getString("agentType", null), readStringOrNull(obj, "knowledgeGraphDomainId"));
    }
}
