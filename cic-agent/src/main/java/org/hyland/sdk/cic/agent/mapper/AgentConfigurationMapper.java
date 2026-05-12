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

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AgentConfigurationMapper implements CICMapper<AgentConfiguration> {

    private final AccessRightMapper accessRightMapper = new AccessRightMapper();

    private final FilterExpressionMapper filterExpressionMapper = new FilterExpressionMapper();

    private final GuardrailMapper guardrailMapper = new GuardrailMapper();

    private final RagParametersMapper ragParametersMapper = new RagParametersMapper();

    @Override
    public AgentConfiguration fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new AgentConfiguration( //
                obj.getStringOrThrow("id"), //
                obj.getStringOrThrow("name"), //
                obj.getStringOrThrow("description"), //
                obj.getStringOrThrow("modelName"), //
                obj.getStringOrNull("avatarUrl"), //
                obj.getStringOrNull("avatarPresignedUrl"), //
                obj.getStringOrNull("instructions"), //
                obj.getOptionalArray("sourceIds").map(CICArray::toListString).orElse(null), //
                obj.getOptionalArray("accessRights")
                   .map(a -> a.toListObject().stream().map(accessRightMapper::fromCICNode).toList())
                   .orElse(null), //
                obj.getInt("version", 0), //
                obj.getBoolean("latest", false), //
                obj.getOptionalObject("staticFilterExpression").map(filterExpressionMapper::fromCICNode).orElse(null), //
                obj.getOptionalObject("dynamicFilterTemplate").map(filterExpressionMapper::fromCICNode).orElse(null), //
                obj.getStringOrNull("agentPlatformAgentId"), //
                obj.getStringOrNull("agentPlatformAgentVersionId"), //
                obj.getOptionalArray("guardrails")
                   .map(a -> a.toListObject().stream().map(guardrailMapper::fromCICNode).toList())
                   .orElse(null), //
                obj.getOptionalObject("ragParameters").map(ragParametersMapper::fromCICNode).orElse(null), //
                obj.getStringOrNull("agentType"), //
                obj.getStringOrNull("knowledgeGraphDomainId"));
    }
}
