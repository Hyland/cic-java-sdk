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

import java.util.List;

/**
 * Represents text embedding vectors returned by the Context API {@code textEmbeddings} action.
 * <p>
 * Each document may produce one or more embedding vectors depending on the chunking strategy. Each inner list contains
 * the floating-point values of a single embedding vector.
 *
 * @param vectors the embedding vectors, one per chunk
 * @since 1.1.0
 */
public record TextEmbedding(List<List<Double>> vectors) implements EnrichmentData {

    public TextEmbedding {
        vectors = vectors == null ? List.of() : List.copyOf(vectors);
    }

    /**
     * Returns the number of embedding vectors (chunks).
     */
    public int size() {
        return vectors.size();
    }

    /**
     * Returns the dimensionality of the embedding vectors, or {@code 0} if empty.
     */
    public int dimensions() {
        return vectors.isEmpty() ? 0 : vectors.get(0).size();
    }
}
