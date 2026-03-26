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
public record IntegrationSubmitQuestionRequest(String question, String userId, List<String> contextObjectIds) {

    public IntegrationSubmitQuestionRequest {
        Objects.requireNonNull(question, "question cannot be null");
        Objects.requireNonNull(userId, "userId cannot be null");
        contextObjectIds = contextObjectIds != null ? Collections.unmodifiableList(contextObjectIds) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String question, String userId) {
        return new Builder(question, userId);
    }

    public static final class Builder {

        private String question;

        private String userId;

        private List<String> contextObjectIds;

        private Builder() {
        }

        private Builder(String question, String userId) {
            this.question = Objects.requireNonNull(question, "question cannot be null");
            this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder contextObjectIds(List<String> contextObjectIds) {
            this.contextObjectIds = contextObjectIds != null ? new ArrayList<>(contextObjectIds) : null;
            return this;
        }

        public IntegrationSubmitQuestionRequest build() {
            return new IntegrationSubmitQuestionRequest(question, userId, contextObjectIds);
        }
    }
}
