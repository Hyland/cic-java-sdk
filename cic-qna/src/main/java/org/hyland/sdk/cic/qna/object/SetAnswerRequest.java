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
public record SetAnswerRequest(String answer, List<SetAnswerReference> references) {

    public SetAnswerRequest {
        Objects.requireNonNull(answer, "answer cannot be null");
        references = references != null ? List.copyOf(references) : List.of();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(String answer) {
        return new Builder(answer);
    }

    public static final class Builder {

        private String answer;

        private List<SetAnswerReference> references;

        private Builder() {
        }

        private Builder(String answer) {
            this.answer = Objects.requireNonNull(answer, "answer cannot be null");
        }

        public Builder answer(String answer) {
            this.answer = answer;
            return this;
        }

        public Builder references(List<SetAnswerReference> references) {
            this.references = references != null ? new ArrayList<>(references) : null;
            return this;
        }

        public SetAnswerRequest build() {
            return new SetAnswerRequest(answer, references);
        }
    }
}
