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

import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.toCICObject;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.writeAccessRights;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.writeGuardrails;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.writeRagParameters;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.writeStringList;

import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class CreateAgentMapper implements CICMapper<CreateAgent> {

    @Override
    public CICNode toCICNode(CreateAgent agent) {
        var obj = CICObject.create();
        obj.putString("name", agent.name());
        obj.putString("description", agent.description());
        obj.putString("modelName", agent.modelName());
        if (agent.avatarUrl() != null) {
            obj.putString("avatarUrl", agent.avatarUrl());
        } else {
            obj.putNull("avatarUrl");
        }
        if (agent.instructions() != null) {
            obj.putString("instructions", agent.instructions());
        } else {
            obj.putNull("instructions");
        }
        if (agent.sourceIds() != null) {
            obj.putArray("sourceIds", writeStringList(agent.sourceIds()));
        } else {
            obj.putNull("sourceIds");
        }
        if (agent.accessRights() != null) {
            obj.putArray("accessRights", writeAccessRights(agent.accessRights()));
        } else {
            obj.putNull("accessRights");
        }
        if (agent.staticFilterExpression() != null) {
            obj.putObject("staticFilterExpression", toCICObject(agent.staticFilterExpression()));
        } else {
            obj.putNull("staticFilterExpression");
        }
        if (agent.dynamicFilterTemplate() != null) {
            obj.putObject("dynamicFilterTemplate", toCICObject(agent.dynamicFilterTemplate()));
        } else {
            obj.putNull("dynamicFilterTemplate");
        }
        if (agent.guardrails() != null) {
            obj.putArray("guardrails", writeGuardrails(agent.guardrails()));
        } else {
            obj.putNull("guardrails");
        }
        if (agent.ragParameters() != null) {
            obj.putObject("ragParameters", writeRagParameters(agent.ragParameters()));
        } else {
            obj.putNull("ragParameters");
        }
        if (agent.agentType() != null) {
            obj.putString("agentType", agent.agentType());
        } else {
            obj.putNull("agentType");
        }
        if (agent.knowledgeGraphDomainId() != null) {
            obj.putString("knowledgeGraphDomainId", agent.knowledgeGraphDomainId());
        } else {
            obj.putNull("knowledgeGraphDomainId");
        }
        return obj;
    }
}
