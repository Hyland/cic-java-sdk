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
package org.hyland.sdk.cic.agent.object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @since 1.0.0
 */
public record AgentSummary(String id, String name, String description, String modelName, String avatarUrl,
        String avatarPresignedUrl, String instructions, List<String> sourceIds, List<AccessRight> accessRights,
        int version, boolean latest, FilterExpression staticFilterExpression, FilterExpression dynamicFilterTemplate,
        String agentType, String knowledgeGraphDomainId) {

    public AgentSummary {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(modelName, "modelName cannot be null");
        sourceIds = sourceIds != null ? Collections.unmodifiableList(sourceIds) : List.of();
        accessRights = accessRights != null ? Collections.unmodifiableList(accessRights) : List.of();
    }

    /**
     * Type-token marker class used for SPI-based mapper dispatch. Extending {@link ArrayList} allows
     * {@link org.hyland.sdk.cic.http.client.mapper.MapperService} to resolve a distinct mapper for a
     * {@code List<AgentSummary>} without relying on generic type information that is erased at runtime.
     */
    public static class ListOf extends ArrayList<AgentSummary> {
    }
}
