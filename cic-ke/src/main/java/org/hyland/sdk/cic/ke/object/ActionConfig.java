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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Per-action configuration for Context API v2 process requests. Each action in the request can carry its own classes,
 * maxWordCount, kSimilarMetadata, and instructions.
 *
 * @since 1.0.0
 */
public final class ActionConfig {

    private static final ActionConfig EMPTY = new ActionConfig(new Builder());

    private final List<String> classes;

    private final Integer maxWordCount;

    private final List<Map<String, Object>> kSimilarMetadata;

    private final Map<String, String> instructions;

    private final String category;

    private final String model;

    private ActionConfig(Builder builder) {
        this.classes = builder.classes == null ? null : List.copyOf(builder.classes);
        this.maxWordCount = builder.maxWordCount;
        this.kSimilarMetadata = builder.kSimilarMetadata == null ? null : List.copyOf(builder.kSimilarMetadata);
        this.instructions = builder.instructions == null ? null : Map.copyOf(builder.instructions);
        this.category = builder.category;
        this.model = builder.model;
    }

    /**
     * Returns a shared empty configuration instance (no classes, no instructions, etc.).
     */
    public static ActionConfig empty() {
        return EMPTY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<String> classes() {
        return classes;
    }

    public Integer maxWordCount() {
        return maxWordCount;
    }

    public List<Map<String, Object>> kSimilarMetadata() {
        return kSimilarMetadata;
    }

    /**
     * Per-action instructions map. Supported by classification and metadata generation actions.
     */
    public Map<String, String> instructions() {
        return instructions;
    }

    public String category() {
        return category;
    }

    public String model() {
        return model;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ActionConfig that = (ActionConfig) obj;
        return Objects.equals(classes, that.classes) && Objects.equals(maxWordCount, that.maxWordCount)
                && Objects.equals(kSimilarMetadata, that.kSimilarMetadata)
                && Objects.equals(instructions, that.instructions) && Objects.equals(category, that.category)
                && Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, maxWordCount, kSimilarMetadata, instructions, category, model);
    }

    public static final class Builder {

        private List<String> classes;

        private Integer maxWordCount;

        private List<Map<String, Object>> kSimilarMetadata;

        private Map<String, String> instructions;

        private String category;

        private String model;

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

        public Builder maxWordCount(int maxWordCount) {
            this.maxWordCount = maxWordCount;
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

        public Builder instructions(Map<String, String> instructions) {
            this.instructions = new LinkedHashMap<>(instructions);
            return this;
        }

        public Builder instruction(String key, String value) {
            if (this.instructions == null)
                this.instructions = new LinkedHashMap<>();
            this.instructions.put(key, value);
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public ActionConfig build() {
            return new ActionConfig(this);
        }
    }
}
