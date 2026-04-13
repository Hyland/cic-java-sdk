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
package org.hyland.sdk.cic.qna.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 1.0.0
 */
public record SubmitQuestionRequest(String questionId, String question, List<String> contextObjectIds, String userId,
        String externalUserId, IntegrationType integrationType, String agentId, int agentVersion, String modelName,
        String instructions, List<String> sourceIds, Filters filters) {

    public SubmitQuestionRequest {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        Objects.requireNonNull(question, "question cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(integrationType, "integrationType cannot be null");
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(modelName, "modelName cannot be null");
        contextObjectIds = contextObjectIds != null ? List.copyOf(contextObjectIds) : null;
        sourceIds = sourceIds != null ? List.copyOf(sourceIds) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String questionId;

        private String question;

        private List<String> contextObjectIds;

        private String userId;

        private String externalUserId;

        private IntegrationType integrationType;

        private String agentId;

        private int agentVersion;

        private String modelName;

        private String instructions;

        private List<String> sourceIds;

        private Filters filters;

        private Builder() {
        }

        public Builder questionId(String questionId) {
            this.questionId = questionId;
            return this;
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder contextObjectIds(List<String> contextObjectIds) {
            this.contextObjectIds = contextObjectIds != null ? new ArrayList<>(contextObjectIds) : null;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder externalUserId(String externalUserId) {
            this.externalUserId = externalUserId;
            return this;
        }

        public Builder integrationType(IntegrationType integrationType) {
            this.integrationType = integrationType;
            return this;
        }

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder agentVersion(int agentVersion) {
            this.agentVersion = agentVersion;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder sourceIds(List<String> sourceIds) {
            this.sourceIds = sourceIds != null ? new ArrayList<>(sourceIds) : null;
            return this;
        }

        public Builder filters(Filters filters) {
            this.filters = filters;
            return this;
        }

        public SubmitQuestionRequest build() {
            return new SubmitQuestionRequest(questionId, question, contextObjectIds, userId, externalUserId,
                    integrationType, agentId, agentVersion, modelName, instructions, sourceIds, filters);
        }
    }
}
