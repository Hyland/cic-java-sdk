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
package org.hyland.sdk.cic.nucleus.mapper;

import java.util.UUID;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.nucleus.object.SystemIntegrationType;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;

/**
 * @since 1.0.0
 */
class SystemOutputMapper implements CICMapper<SystemOutput> {

    @Override
    public SystemOutput fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var systemIdStr = obj.getString("systemId", null);
        var environmentIdStr = obj.getString("environmentId", null);
        var systemTypeStr = obj.getString("systemType", null);
        return new SystemOutput(systemIdStr != null ? UUID.fromString(systemIdStr) : null, obj.getString("name", null),
                environmentIdStr != null ? UUID.fromString(environmentIdStr) : null,
                systemTypeStr != null ? SystemIntegrationType.fromValue(systemTypeStr) : null);
    }

    static class PaginatedListMapper implements CICMapper<SystemOutput.PaginatedListOf> {

        private final SystemOutputMapper innerMapper = new SystemOutputMapper();

        @Override
        public SystemOutput.PaginatedListOf fromCICNode(CICNode cicNode) {
            if (!(cicNode instanceof CICObject obj)) {
                throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
            }
            var items = obj.getOptionalArray("items")
                           .map(a -> a.toListObject().stream().map(innerMapper::fromCICNode).toList())
                           .orElse(null);
            var next = obj.getString("next", null);
            return new SystemOutput.PaginatedListOf(items, next);
        }
    }
}
