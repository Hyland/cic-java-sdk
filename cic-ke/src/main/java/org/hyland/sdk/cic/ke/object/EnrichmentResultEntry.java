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
import java.util.Map;

/**
 * Per-object enrichment result entry from the Context API.
 *
 * @since 1.1.0
 */
public record EnrichmentResultEntry(String objectKey, ActionResult<String> imageDescription,
        ActionResult<Map<String, Object>> imageMetadata, ActionResult<Map<String, Object>> textMetadata,
        ActionResult<String> textSummary, ActionResult<String> textClassification,
        ActionResult<String> imageClassification, ActionResult<List<List<Double>>> textEmbeddings,
        ActionResult<List<Double>> imageEmbeddings, ActionResult<Map<String, List<String>>> namedEntityText,
        ActionResult<Map<String, List<String>>> namedEntityImage,
        ActionResult<ClassificationResult> pretrainedClassification, List<ProcessingError> generalProcessingErrors) {

    public EnrichmentResultEntry {
        generalProcessingErrors = generalProcessingErrors == null ? List.of() : List.copyOf(generalProcessingErrors);
    }
}
