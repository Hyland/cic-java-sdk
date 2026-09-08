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

/**
 * Embedding model information from {@code GET /models} endpoint.
 *
 * @since 1.1.0
 */
public record EmbeddingModel(String name, int maxChunkSize, List<String> supportedPrecisions,
        List<Integer> supportedOutputDimensions, List<String> supportedInputType) {

    public EmbeddingModel {
        supportedPrecisions = supportedPrecisions == null ? List.of() : List.copyOf(supportedPrecisions);
        supportedOutputDimensions = supportedOutputDimensions == null ? List.of()
                : List.copyOf(supportedOutputDimensions);
        supportedInputType = supportedInputType == null ? List.of() : List.copyOf(supportedInputType);
    }

    /**
     * @since 1.1.0
     */
    public static class ListOf extends ArrayList<EmbeddingModel> {
    }
}
