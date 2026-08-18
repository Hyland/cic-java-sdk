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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @since 1.0.0
 */
public final class ProcessRequest {

    private final List<ObjectKeyPath> objectKeys;

    private final List<String> actions;

    private final List<String> classes;

    private final List<Map<String, Object>> kSimilarMetadata;

    private final Integer maxWordCount;

    private final String instructions;

    private final String extraJsonPayload;

    private ProcessRequest(Builder builder) {
        this.objectKeys = List.copyOf(builder.objectKeys);
        this.actions = builder.actions == null ? List.of() : List.copyOf(builder.actions);
        this.classes = builder.classes == null ? null : List.copyOf(builder.classes);
        this.kSimilarMetadata = builder.kSimilarMetadata == null ? null : List.copyOf(builder.kSimilarMetadata);
        this.maxWordCount = builder.maxWordCount;
        this.instructions = builder.instructions;
        this.extraJsonPayload = builder.extraJsonPayload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<ObjectKeyPath> objectKeys() {
        return objectKeys;
    }

    public List<String> actions() {
        return actions;
    }

    public List<String> classes() {
        return classes;
    }

    public List<Map<String, Object>> kSimilarMetadata() {
        return kSimilarMetadata;
    }

    public Integer maxWordCount() {
        return maxWordCount;
    }

    /**
     * Optional instructions for the model. Supported by all actions except embedding actions.
     */
    public String instructions() {
        return instructions;
    }

    public String extraJsonPayload() {
        return extraJsonPayload;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProcessRequest that = (ProcessRequest) obj;
        return Objects.equals(objectKeys, that.objectKeys) && Objects.equals(actions, that.actions)
                && Objects.equals(classes, that.classes) && Objects.equals(kSimilarMetadata, that.kSimilarMetadata)
                && Objects.equals(maxWordCount, that.maxWordCount) && Objects.equals(instructions, that.instructions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectKeys, actions, classes, kSimilarMetadata, maxWordCount, instructions);
    }

    public static final class Builder {

        private final List<ObjectKeyPath> objectKeys = new ArrayList<>();

        private List<String> actions;

        private List<String> classes;

        private List<Map<String, Object>> kSimilarMetadata;

        private Integer maxWordCount;

        private String instructions;

        private String extraJsonPayload;

        public Builder objectKey(String path) {
            objectKeys.add(new ObjectKeyPath(path));
            return this;
        }

        public Builder objectKeys(List<ObjectKeyPath> objectKeys) {
            this.objectKeys.addAll(objectKeys);
            return this;
        }

        public Builder action(String action) {
            if (this.actions == null)
                this.actions = new ArrayList<>();
            this.actions.add(action);
            return this;
        }

        public Builder action(Action action) {
            return action(action.value());
        }

        public Builder actions(List<String> actions) {
            this.actions = new ArrayList<>(actions);
            return this;
        }

        public Builder classes(List<String> classes) {
            this.classes = new ArrayList<>(classes);
            return this;
        }

        public Builder addClass(String clazz) {
            if (this.classes == null)
                this.classes = new ArrayList<>();
            this.classes.add(clazz);
            return this;
        }

        public Builder kSimilarMetadata(List<Map<String, Object>> kSimilarMetadata) {
            this.kSimilarMetadata = new ArrayList<>(kSimilarMetadata);
            return this;
        }

        public Builder addSimilarMetadata(Map<String, Object> metadata) {
            if (this.kSimilarMetadata == null)
                this.kSimilarMetadata = new ArrayList<>();
            this.kSimilarMetadata.add(metadata);
            return this;
        }

        public Builder maxWordCount(int maxWordCount) {
            this.maxWordCount = maxWordCount;
            return this;
        }

        /**
         * Sets optional instructions for the model. Supported by all actions except embedding actions.
         */
        public Builder instructions(String instructions) {
            this.instructions = instructions;
            return this;
        }

        public Builder extraJsonPayload(String extraJsonPayload) {
            this.extraJsonPayload = extraJsonPayload;
            return this;
        }

        public ProcessRequest build() {
            if (objectKeys.isEmpty()) {
                throw new IllegalArgumentException("At least one objectKey is required");
            }
            return new ProcessRequest(this);
        }
    }
}
