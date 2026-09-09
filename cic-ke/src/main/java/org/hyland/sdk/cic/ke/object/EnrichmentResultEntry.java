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
 * Per-object enrichment result entry from the Context API.
 *
 * @since 1.1.0
 */
public record EnrichmentResultEntry(String objectKey, ActionResult<ImageDescription> imageDescription,
        ActionResult<ImageMetadata> imageMetadata, ActionResult<TextMetadata> textMetadata,
        ActionResult<TextSummary> textSummary, ActionResult<TextClassificationResult> textClassification,
        ActionResult<ImageClassificationResult> imageClassification, ActionResult<TextEmbedding> textEmbeddings,
        ActionResult<ImageEmbedding> imageEmbeddings, ActionResult<NamedEntities> namedEntityText,
        ActionResult<NamedEntities> namedEntityImage, ActionResult<ClassificationResult> pretrainedClassification,
        List<ProcessingError> generalProcessingErrors) {

    public EnrichmentResultEntry {
        generalProcessingErrors = generalProcessingErrors == null ? List.of() : List.copyOf(generalProcessingErrors);
    }
}
