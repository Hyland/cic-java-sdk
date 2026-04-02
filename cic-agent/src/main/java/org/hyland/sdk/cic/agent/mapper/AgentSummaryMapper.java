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
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readStringList;
import static org.hyland.sdk.cic.agent.mapper.AgentMapperUtils.readStringOrNull;

import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.AgentSummary.ListOf;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AgentSummaryMapper implements CICMapper<AgentSummary> {

    @Override
    public AgentSummary fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new AgentSummary(obj.getStringOrThrow("id"), obj.getStringOrThrow("name"),
                obj.getStringOrThrow("description"), obj.getStringOrThrow("modelName"),
                obj.getString("avatarUrl", null), obj.getString("avatarPresignedUrl", null),
                obj.getString("instructions", null), readStringList(obj, "sourceIds"), readAccessRights(obj),
                obj.getInt("version", 0), obj.getBoolean("latest", false),
                readFilterExpressionOrNull(obj, "staticFilterExpression"),
                readFilterExpressionOrNull(obj, "dynamicFilterTemplate"), obj.getString("agentType", null),
                readStringOrNull(obj, "knowledgeGraphDomainId"));
    }

    static class ListMapper implements CICMapper<ListOf> {

        private final AgentSummaryMapper innerMapper = new AgentSummaryMapper();

        @Override
        public ListOf fromCICNode(CICNode cicNode) {
            if (!(cicNode instanceof CICArray cicArray)) {
                throw new IllegalArgumentException("Expected CICArray, got: " + cicNode.getClass().getSimpleName());
            }
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(ListOf::new));
        }
    }
}
