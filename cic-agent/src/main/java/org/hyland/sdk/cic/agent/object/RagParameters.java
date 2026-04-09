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

/**
 * @since 1.0.0
 */
public record RagParameters(Integer limit, Integer adjacentChunkRange, Boolean adjacentChunkMerge,
        Boolean rerankerEnabled, Integer rerankerTopN) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Integer limit;

        private Integer adjacentChunkRange;

        private Boolean adjacentChunkMerge;

        private Boolean rerankerEnabled;

        private Integer rerankerTopN;

        private Builder() {
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder adjacentChunkRange(Integer adjacentChunkRange) {
            this.adjacentChunkRange = adjacentChunkRange;
            return this;
        }

        public Builder adjacentChunkMerge(Boolean adjacentChunkMerge) {
            this.adjacentChunkMerge = adjacentChunkMerge;
            return this;
        }

        public Builder rerankerEnabled(Boolean rerankerEnabled) {
            this.rerankerEnabled = rerankerEnabled;
            return this;
        }

        public Builder rerankerTopN(Integer rerankerTopN) {
            this.rerankerTopN = rerankerTopN;
            return this;
        }

        public RagParameters build() {
            return new RagParameters(limit, adjacentChunkRange, adjacentChunkMerge, rerankerEnabled, rerankerTopN);
        }
    }
}
