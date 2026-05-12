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

import org.hyland.sdk.cic.agent.object.GuardrailDefinition;
import org.hyland.sdk.cic.agent.object.GuardrailGroup;
import org.hyland.sdk.cic.agent.object.GuardrailsResponse;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class GuardrailsResponseMapper implements CICMapper<GuardrailsResponse> {

    @Override
    public GuardrailsResponse fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var groups = obj.getOptionalArray("guardrailGroups")
                        .map(a -> a.toListObject().stream().map(this::readGuardrailGroup).toList())
                        .orElse(null);
        return new GuardrailsResponse(groups);
    }

    private GuardrailGroup readGuardrailGroup(CICObject obj) {
        var displayName = obj.getStringOrThrow("displayName");
        var description = obj.getStringOrThrow("description");
        var guardrails = obj.getOptionalArray("guardrails")
                            .map(a -> a.toListObject().stream().map(this::readGuardrailDefinition).toList())
                            .orElse(null);
        return new GuardrailGroup(displayName, description, guardrails);
    }

    private GuardrailDefinition readGuardrailDefinition(CICObject obj) {
        return new GuardrailDefinition(obj.getStringOrThrow("name"), obj.getStringOrNull("severity"),
                obj.getBoolean("isRecommended", false));
    }
}
