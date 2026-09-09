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
package org.hyland.sdk.cic.ke.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.ActionResult;
import org.hyland.sdk.cic.ke.object.ClassificationResult;
import org.hyland.sdk.cic.ke.object.EnrichmentData;
import org.hyland.sdk.cic.ke.object.EnrichmentResultEntry;
import org.hyland.sdk.cic.ke.object.ImageClassificationResult;
import org.hyland.sdk.cic.ke.object.ImageDescription;
import org.hyland.sdk.cic.ke.object.ImageEmbedding;
import org.hyland.sdk.cic.ke.object.ImageMetadata;
import org.hyland.sdk.cic.ke.object.NamedEntities;
import org.hyland.sdk.cic.ke.object.TextClassificationResult;
import org.hyland.sdk.cic.ke.object.TextEmbedding;
import org.hyland.sdk.cic.ke.object.TextMetadata;
import org.hyland.sdk.cic.ke.object.TextSummary;

/**
 * @since 1.1.0
 */
class EnrichmentResultEntryMapper {

    private final ProcessingErrorMapper errorMapper = new ProcessingErrorMapper();

    private final ClassificationResultMapper classificationMapper = new ClassificationResultMapper();

    EnrichmentResultEntry fromCICObject(CICObject obj) {
        return new EnrichmentResultEntry(obj.getStringOrNull("objectKey"),
                mapWrappedStringActionResult(obj, "imageDescription", ImageDescription::new),
                mapMetadataActionResult(obj, "imageMetadata", ImageMetadata::new),
                mapMetadataActionResult(obj, "textMetadata", TextMetadata::new),
                mapWrappedStringActionResult(obj, "textSummary", TextSummary::new),
                mapWrappedStringActionResult(obj, "textClassification", TextClassificationResult::new),
                mapWrappedStringActionResult(obj, "imageClassification", ImageClassificationResult::new),
                mapTextEmbeddingsActionResult(obj), mapImageEmbeddingsActionResult(obj),
                mapNamedEntitiesActionResult(obj, "namedEntityText"),
                mapNamedEntitiesActionResult(obj, "namedEntityImage"),
                mapClassificationActionResult(obj, "pretrainedClassification"),
                errorMapper.mapGeneralProcessingErrors(obj));
    }

    private <T extends EnrichmentData> ActionResult<T> mapWrappedStringActionResult(CICObject parent, String key,
            Function<String, T> wrapper) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var result = obj.getStringOrNull("result");
            var error = errorMapper.mapActionError(obj);
            if (isSuccess) {
                return ActionResult.<T> success(result != null ? wrapper.apply(result) : null);
            }
            return ActionResult.<T> failure(error);
        }).orElse(null);
    }

    private <T extends EnrichmentData> ActionResult<T> mapMetadataActionResult(CICObject parent, String key,
            Function<Map<String, Object>, T> wrapper) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            T result = obj.getOptionalObject("result").map(r -> wrapper.apply(r.toMap())).orElse(null);
            if (isSuccess) {
                return ActionResult.<T> success(result);
            }
            return ActionResult.<T> failure(error);
        }).orElse(null);
    }

    private ActionResult<TextEmbedding> mapTextEmbeddingsActionResult(CICObject parent) {
        return parent.getOptionalObject("textEmbeddings").map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            TextEmbedding result = null;
            var resultArr = obj.getOptionalArray("result").orElse(null);
            if (resultArr != null) {
                List<List<Double>> vectors;
                if (resultArr.getElements().isEmpty()) {
                    vectors = List.of();
                } else if (resultArr.getElements().get(0) instanceof CICArray) {
                    var parsed = new ArrayList<List<Double>>();
                    for (var element : resultArr.getElements()) {
                        if (!(element instanceof CICArray vector)) {
                            throw invalidEmbeddingShape("textEmbeddings", "mixed vectors and scalar values");
                        }
                        parsed.add(mapVector(vector, "textEmbeddings"));
                    }
                    vectors = List.copyOf(parsed);
                } else {
                    vectors = List.of(mapVector(resultArr, "textEmbeddings"));
                }
                result = new TextEmbedding(vectors);
            }
            if (isSuccess) {
                return ActionResult.<TextEmbedding> success(result);
            }
            return ActionResult.<TextEmbedding> failure(error);
        }).orElse(null);
    }

    private ActionResult<ImageEmbedding> mapImageEmbeddingsActionResult(CICObject parent) {
        return parent.getOptionalObject("imageEmbeddings").map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            ImageEmbedding result = null;
            var resultArr = obj.getOptionalArray("result").orElse(null);
            if (resultArr != null) {
                List<Double> vector;
                if (resultArr.getElements().size() == 1 && resultArr.getElements().get(0) instanceof CICArray inner) {
                    vector = mapVector(inner, "imageEmbeddings");
                } else if (resultArr.getElements().stream().anyMatch(CICArray.class::isInstance)) {
                    throw invalidEmbeddingShape("imageEmbeddings", "expected one vector");
                } else {
                    vector = mapVector(resultArr, "imageEmbeddings");
                }
                result = new ImageEmbedding(vector);
            }
            if (isSuccess) {
                return ActionResult.<ImageEmbedding> success(result);
            }
            return ActionResult.<ImageEmbedding> failure(error);
        }).orElse(null);
    }

    private List<Double> mapVector(CICArray vector, String actionName) {
        var values = new ArrayList<Double>();
        for (var element : vector.getElements()) {
            if (element instanceof CICPrimitive.CICDouble d) {
                values.add(d.value());
            } else if (element instanceof CICPrimitive.CICLong l) {
                values.add((double) l.value());
            } else if (element instanceof CICPrimitive.CICInt i) {
                values.add((double) i.value());
            } else {
                throw invalidEmbeddingShape(actionName, "vector contains a non-numeric value");
            }
        }
        return List.copyOf(values);
    }

    private CICSdkException invalidEmbeddingShape(String actionName, String reason) {
        return new CICSdkException("Invalid " + actionName + " result: " + reason);
    }

    private ActionResult<NamedEntities> mapNamedEntitiesActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            NamedEntities result = obj.getOptionalObject("result").map(resultObj -> {
                var entities = new HashMap<String, List<String>>();
                for (var entry : resultObj.getProperties().entrySet()) {
                    if (entry.getValue() instanceof CICArray arr) {
                        entities.put(entry.getKey(), arr.toListString());
                    }
                }
                return new NamedEntities(entities);
            }).orElse(null);
            if (isSuccess) {
                return ActionResult.<NamedEntities> success(result);
            }
            return ActionResult.<NamedEntities> failure(error);
        }).orElse(null);
    }

    private ActionResult<ClassificationResult> mapClassificationActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            ClassificationResult result = obj.getOptionalObject("result")
                                             .map(classificationMapper::fromCICObject)
                                             .orElse(null);
            if (isSuccess) {
                return ActionResult.<ClassificationResult> success(result);
            }
            return ActionResult.<ClassificationResult> failure(error);
        }).orElse(null);
    }
}
