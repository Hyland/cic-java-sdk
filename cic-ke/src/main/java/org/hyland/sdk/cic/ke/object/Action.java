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

/**
 * Context API v2 action types using camelCase naming.
 *
 * @since 1.0.0
 */
public enum Action {

    IMAGE_CLASSIFICATION("imageClassification"), IMAGE_DESCRIPTION("imageDescription"), IMAGE_EMBEDDINGS(
            "imageEmbeddings"), IMAGE_METADATA_GENERATION("imageMetadataGeneration"), NAMED_ENTITY_RECOGNITION_IMAGE(
                    "namedEntityRecognitionImage"), NAMED_ENTITY_RECOGNITION_TEXT(
                            "namedEntityRecognitionText"), TEXT_CLASSIFICATION("textClassification"), TEXT_EMBEDDINGS(
                                    "textEmbeddings"), TEXT_METADATA_GENERATION(
                                            "textMetadataGeneration"), TEXT_SUMMARIZATION("textSummarization");

    private final String value;

    Action(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
