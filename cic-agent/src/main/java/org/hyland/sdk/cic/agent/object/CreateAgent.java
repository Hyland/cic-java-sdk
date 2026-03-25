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
import java.util.UUID;

import org.hyland.sdk.cic.http.client.mapper.object.CICNode;

/**
 * @since 1.0.0
 */
public record CreateAgent(String name, String description, String modelName, String avatarUrl, String instructions,
        List<UUID> sourceIds, List<AccessRight> accessRights, CICNode staticFilterExpression,
        CICNode dynamicFilterTemplate, List<Guardrail> guardrails, RagParameters ragParameters, String agentType,
        UUID knowledgeGraphDomainId) {

    public CreateAgent {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
        Objects.requireNonNull(modelName, "modelName cannot be null");
        sourceIds = sourceIds != null ? Collections.unmodifiableList(sourceIds) : null;
        accessRights = accessRights != null ? Collections.unmodifiableList(accessRights) : null;
        guardrails = guardrails != null ? Collections.unmodifiableList(guardrails) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String name, String description, String modelName) {
        return new Builder(name, description, modelName);
    }

    public static final class Builder {

        private String name;

        private String description;

        private String modelName;

        private String avatarUrl;

        private String instructions;

        private List<UUID> sourceIds;

        private List<AccessRight> accessRights;

        private CICNode staticFilterExpression;

        private CICNode dynamicFilterTemplate;

        private List<Guardrail> guardrails;

        private RagParameters ragParameters;

        private String agentType;

        private UUID knowledgeGraphDomainId;

        private Builder() {
        }

        private Builder(String name, String description, String modelName) {
            this.name = Objects.requireNonNull(name, "name cannot be null");
            this.description = Objects.requireNonNull(description, "description cannot be null");
            this.modelName = Objects.requireNonNull(modelName, "modelName cannot be null");
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder sourceIds(List<UUID> sourceIds) {
            this.sourceIds = sourceIds != null ? new ArrayList<>(sourceIds) : null;
            return this;
        }

        public Builder accessRights(List<AccessRight> accessRights) {
            this.accessRights = accessRights != null ? new ArrayList<>(accessRights) : null;
            return this;
        }

        public Builder staticFilterExpression(CICNode staticFilterExpression) {
            this.staticFilterExpression = staticFilterExpression;
            return this;
        }

        public Builder dynamicFilterTemplate(CICNode dynamicFilterTemplate) {
            this.dynamicFilterTemplate = dynamicFilterTemplate;
            return this;
        }

        public Builder guardrails(List<Guardrail> guardrails) {
            this.guardrails = guardrails != null ? new ArrayList<>(guardrails) : null;
            return this;
        }

        public Builder ragParameters(RagParameters ragParameters) {
            this.ragParameters = ragParameters;
            return this;
        }

        public Builder agentType(String agentType) {
            this.agentType = agentType;
            return this;
        }

        public Builder knowledgeGraphDomainId(UUID knowledgeGraphDomainId) {
            this.knowledgeGraphDomainId = knowledgeGraphDomainId;
            return this;
        }

        public CreateAgent build() {
            return new CreateAgent(name, description, modelName, avatarUrl, instructions, sourceIds, accessRights,
                    staticFilterExpression, dynamicFilterTemplate, guardrails, ragParameters, agentType,
                    knowledgeGraphDomainId);
        }
    }
}
