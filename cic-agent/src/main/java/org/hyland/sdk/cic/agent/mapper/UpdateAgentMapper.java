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

import org.hyland.sdk.cic.agent.object.UpdateAgent;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class UpdateAgentMapper implements CICMapper<UpdateAgent> {

    private final AccessRightMapper accessRightMapper = new AccessRightMapper();

    private final FilterExpressionMapper filterExpressionMapper = new FilterExpressionMapper();

    private final GuardrailMapper guardrailMapper = new GuardrailMapper();

    private final RagParametersMapper ragParametersMapper = new RagParametersMapper();

    @Override
    public CICNode toCICNode(UpdateAgent agent) {
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
            obj.putArray("sourceIds", CICArray.from(agent.sourceIds()));
        } else {
            obj.putNull("sourceIds");
        }
        if (agent.accessRights() != null) {
            obj.putArray("accessRights",
                    CICArray.from(agent.accessRights().stream().map(accessRightMapper::toCICNode).toList()));
        } else {
            obj.putNull("accessRights");
        }
        if (agent.staticFilterExpression() != null) {
            obj.putObject("staticFilterExpression", filterExpressionMapper.toCICNode(agent.staticFilterExpression()));
        } else {
            obj.putNull("staticFilterExpression");
        }
        if (agent.dynamicFilterTemplate() != null) {
            obj.putObject("dynamicFilterTemplate", filterExpressionMapper.toCICNode(agent.dynamicFilterTemplate()));
        } else {
            obj.putNull("dynamicFilterTemplate");
        }
        if (agent.guardrails() != null) {
            obj.putArray("guardrails",
                    CICArray.from(agent.guardrails().stream().map(guardrailMapper::toCICNode).toList()));
        } else {
            obj.putNull("guardrails");
        }
        if (agent.ragParameters() != null) {
            obj.putObject("ragParameters", ragParametersMapper.toCICNode(agent.ragParameters()));
        } else {
            obj.putNull("ragParameters");
        }
        if (agent.knowledgeGraphDomainId() != null) {
            obj.putString("knowledgeGraphDomainId", agent.knowledgeGraphDomainId());
        } else {
            obj.putNull("knowledgeGraphDomainId");
        }
        return obj;
    }
}
