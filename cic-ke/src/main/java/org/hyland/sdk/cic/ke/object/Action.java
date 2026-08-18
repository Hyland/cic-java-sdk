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
 * @since 1.0.0
 */
public enum Action {

    IMAGE_CLASSIFICATION("image-classification"), IMAGE_DESCRIPTION("image-description"), IMAGE_EMBEDDINGS(
            "image-embeddings"), IMAGE_METADATA_GENERATION("image-metadata-generation"), NAMED_ENTITY_RECOGNITION_IMAGE(
                    "named-entity-recognition-image"), NAMED_ENTITY_RECOGNITION_TEXT(
                            "named-entity-recognition-text"), TEXT_CLASSIFICATION(
                                    "text-classification"), TEXT_EMBEDDINGS(
                                            "text-embeddings"), TEXT_METADATA_GENERATION(
                                                    "text-metadata-generation"), TEXT_SUMMARIZATION(
                                                            "text-summarization");

    private final String value;

    Action(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
