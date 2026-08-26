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
 * @since 1.0.0
 */
public final class EnrichmentResultEntry {

    private final String objectKey;

    private final ActionResult<String> imageDescription;

    private final ActionResult<Map<String, Object>> imageMetadata;

    private final ActionResult<Map<String, Object>> textMetadata;

    private final ActionResult<String> textSummary;

    private final ActionResult<String> textClassification;

    private final ActionResult<String> imageClassification;

    private final ActionResult<List<List<Double>>> textEmbeddings;

    private final ActionResult<List<Double>> imageEmbeddings;

    private final ActionResult<Map<String, List<String>>> namedEntityText;

    private final ActionResult<Map<String, List<String>>> namedEntityImage;

    private final ActionResult<ClassificationResult> pretrainedClassification;

    private final List<ProcessingError> generalProcessingErrors;

    public EnrichmentResultEntry(String objectKey, ActionResult<String> imageDescription,
            ActionResult<Map<String, Object>> imageMetadata, ActionResult<Map<String, Object>> textMetadata,
            ActionResult<String> textSummary, ActionResult<String> textClassification,
            ActionResult<String> imageClassification, ActionResult<List<List<Double>>> textEmbeddings,
            ActionResult<List<Double>> imageEmbeddings, ActionResult<Map<String, List<String>>> namedEntityText,
            ActionResult<Map<String, List<String>>> namedEntityImage,
            ActionResult<ClassificationResult> pretrainedClassification,
            List<ProcessingError> generalProcessingErrors) {
        this.objectKey = objectKey;
        this.imageDescription = imageDescription;
        this.imageMetadata = imageMetadata;
        this.textMetadata = textMetadata;
        this.textSummary = textSummary;
        this.textClassification = textClassification;
        this.imageClassification = imageClassification;
        this.textEmbeddings = textEmbeddings;
        this.imageEmbeddings = imageEmbeddings;
        this.namedEntityText = namedEntityText;
        this.namedEntityImage = namedEntityImage;
        this.pretrainedClassification = pretrainedClassification;
        this.generalProcessingErrors = generalProcessingErrors == null ? List.of()
                : List.copyOf(generalProcessingErrors);
    }

    public String objectKey() {
        return objectKey;
    }

    public ActionResult<String> imageDescription() {
        return imageDescription;
    }

    public ActionResult<Map<String, Object>> imageMetadata() {
        return imageMetadata;
    }

    public ActionResult<Map<String, Object>> textMetadata() {
        return textMetadata;
    }

    public ActionResult<String> textSummary() {
        return textSummary;
    }

    public ActionResult<String> textClassification() {
        return textClassification;
    }

    public ActionResult<String> imageClassification() {
        return imageClassification;
    }

    public ActionResult<List<List<Double>>> textEmbeddings() {
        return textEmbeddings;
    }

    public ActionResult<List<Double>> imageEmbeddings() {
        return imageEmbeddings;
    }

    public ActionResult<Map<String, List<String>>> namedEntityText() {
        return namedEntityText;
    }

    public ActionResult<Map<String, List<String>>> namedEntityImage() {
        return namedEntityImage;
    }

    public ActionResult<ClassificationResult> pretrainedClassification() {
        return pretrainedClassification;
    }

    public List<ProcessingError> generalProcessingErrors() {
        return generalProcessingErrors;
    }
}
