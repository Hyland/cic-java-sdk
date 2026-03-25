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

import java.util.UUID;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.AgentAvatar;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AgentAvatarMapper implements CICMapper<AgentAvatar> {

    @Override
    public AgentAvatar fromCICNode(CICNode cicNode) {
        var obj = (CICObject) cicNode;
        var agentIdStr = obj.getString("agentId", null);
        var agentId = agentIdStr != null ? UUID.fromString(agentIdStr) : null;
        return new AgentAvatar(agentId, obj.getString("avatarUrl", null));
    }

    static class ListMapper implements CICMapper<AgentAvatar.List> {

        private final AgentAvatarMapper innerMapper = new AgentAvatarMapper();

        @Override
        public AgentAvatar.List fromCICNode(CICNode cicNode) {
            var cicArray = (CICArray) cicNode;
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(AgentAvatar.List::new));
        }
    }
}
