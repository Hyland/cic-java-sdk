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
public record StartConversationRequest(String question, List<String> contextObjectIds, FilterExpression dynamicFilter) {

    public StartConversationRequest {
        Objects.requireNonNull(question, "question cannot be null");
        contextObjectIds = contextObjectIds != null ? List.copyOf(contextObjectIds) : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String question) {
        return new Builder(question);
    }

    public static final class Builder {

        private String question;

        private List<String> contextObjectIds;

        private FilterExpression dynamicFilter;

        private Builder() {
        }

        private Builder(String question) {
            this.question = Objects.requireNonNull(question, "question cannot be null");
        }

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder contextObjectIds(List<String> contextObjectIds) {
            this.contextObjectIds = contextObjectIds != null ? new ArrayList<>(contextObjectIds) : null;
            return this;
        }

        public Builder dynamicFilter(FilterExpression dynamicFilter) {
            this.dynamicFilter = dynamicFilter;
            return this;
        }

        public StartConversationRequest build() {
            return new StartConversationRequest(question, contextObjectIds, dynamicFilter);
        }
    }
}
