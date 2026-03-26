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

import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.StaticAvatar;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class StaticAvatarMapper implements CICMapper<StaticAvatar> {

    @Override
    public StaticAvatar fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new StaticAvatar(obj.getString("fileName", null), obj.getString("preSignedUrl", null));
    }

    static class ListMapper implements CICMapper<StaticAvatar.List> {

        private final StaticAvatarMapper innerMapper = new StaticAvatarMapper();

        @Override
        public StaticAvatar.List fromCICNode(CICNode cicNode) {
            if (!(cicNode instanceof CICArray cicArray)) {
                throw new IllegalArgumentException("Expected CICArray, got: " + cicNode.getClass().getSimpleName());
            }
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(StaticAvatar.List::new));
        }
    }
}
