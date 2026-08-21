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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Shared processing options used by both Data Curation presign and Configuration API.
 *
 * @since 1.0.0
 */
public final class ProcessingOptions {

    private final NormalizationOptions normalization;

    private final Boolean chunking;

    private final String chunkingStrategy;

    private final Integer chunkSize;

    private final Boolean embedding;

    private final String embeddingsModel;

    private final String embeddingPrecision;

    private final Object jsonSchema;

    private final PiiOptions pii;

    private ProcessingOptions(Builder builder) {
        this.normalization = builder.normalization;
        this.chunking = builder.chunking;
        this.chunkingStrategy = builder.chunkingStrategy;
        this.chunkSize = builder.chunkSize;
        this.embedding = builder.embedding;
        this.embeddingsModel = builder.embeddingsModel;
        this.embeddingPrecision = builder.embeddingPrecision;
        this.jsonSchema = builder.jsonSchema;
        this.pii = builder.pii;
    }

    public static Builder builder() {
        return new Builder();
    }

    public NormalizationOptions normalization() {
        return normalization;
    }

    public Boolean chunking() {
        return chunking;
    }

    public String chunkingStrategy() {
        return chunkingStrategy;
    }

    public Integer chunkSize() {
        return chunkSize;
    }

    public Boolean embedding() {
        return embedding;
    }

    public String embeddingsModel() {
        return embeddingsModel;
    }

    public String embeddingPrecision() {
        return embeddingPrecision;
    }

    public Object jsonSchema() {
        return jsonSchema;
    }

    public PiiOptions pii() {
        return pii;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ProcessingOptions that = (ProcessingOptions) obj;
        return Objects.equals(normalization, that.normalization) && Objects.equals(chunking, that.chunking)
                && Objects.equals(chunkingStrategy, that.chunkingStrategy) && Objects.equals(chunkSize, that.chunkSize)
                && Objects.equals(embedding, that.embedding) && Objects.equals(embeddingsModel, that.embeddingsModel)
                && Objects.equals(embeddingPrecision, that.embeddingPrecision)
                && Objects.equals(jsonSchema, that.jsonSchema) && Objects.equals(pii, that.pii);
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalization, chunking, chunkingStrategy, chunkSize, embedding, embeddingsModel,
                embeddingPrecision, jsonSchema, pii);
    }

    public static final class Builder {

        private NormalizationOptions normalization;

        private Boolean chunking;

        private String chunkingStrategy;

        private Integer chunkSize;

        private Boolean embedding;

        private String embeddingsModel;

        private String embeddingPrecision;

        private Object jsonSchema;

        private PiiOptions pii;

        public Builder normalization(NormalizationOptions normalization) {
            this.normalization = normalization;
            return this;
        }

        public Builder normalization(Consumer<NormalizationOptions.Builder> consumer) {
            var b = NormalizationOptions.builder();
            consumer.accept(b);
            this.normalization = b.build();
            return this;
        }

        public Builder chunking(boolean chunking) {
            this.chunking = chunking;
            return this;
        }

        public Builder chunkingStrategy(String chunkingStrategy) {
            this.chunkingStrategy = chunkingStrategy;
            return this;
        }

        public Builder chunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder embedding(boolean embedding) {
            this.embedding = embedding;
            return this;
        }

        public Builder embeddingsModel(String embeddingsModel) {
            this.embeddingsModel = embeddingsModel;
            return this;
        }

        public Builder embeddingPrecision(String embeddingPrecision) {
            this.embeddingPrecision = embeddingPrecision;
            return this;
        }

        public Builder jsonSchema(String jsonSchema) {
            this.jsonSchema = jsonSchema;
            return this;
        }

        public Builder jsonSchema(boolean jsonSchema) {
            this.jsonSchema = jsonSchema;
            return this;
        }

        public Builder pii(PiiOptions pii) {
            this.pii = pii;
            return this;
        }

        public Builder pii(Consumer<PiiOptions.Builder> consumer) {
            var b = PiiOptions.builder();
            consumer.accept(b);
            this.pii = b.build();
            return this;
        }

        public ProcessingOptions build() {
            return new ProcessingOptions(this);
        }
    }
}
