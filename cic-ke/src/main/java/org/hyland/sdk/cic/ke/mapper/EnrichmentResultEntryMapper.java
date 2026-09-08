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

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.ActionResult;
import org.hyland.sdk.cic.ke.object.ClassificationResult;
import org.hyland.sdk.cic.ke.object.EnrichmentResultEntry;

/**
 * @since 1.1.0
 */
class EnrichmentResultEntryMapper {

    private final ProcessingErrorMapper errorMapper = new ProcessingErrorMapper();

    private final ClassificationResultMapper classificationMapper = new ClassificationResultMapper();

    EnrichmentResultEntry fromCICObject(CICObject obj) {
        return new EnrichmentResultEntry(obj.getStringOrNull("objectKey"),
                mapStringActionResult(obj, "imageDescription"), mapMapActionResult(obj, "imageMetadata"),
                mapMapActionResult(obj, "textMetadata"), mapStringActionResult(obj, "textSummary"),
                mapStringActionResult(obj, "textClassification"), mapStringActionResult(obj, "imageClassification"),
                mapTextEmbeddingsActionResult(obj), mapImageEmbeddingsActionResult(obj),
                mapStringListMapActionResult(obj, "namedEntityText"),
                mapStringListMapActionResult(obj, "namedEntityImage"),
                mapClassificationActionResult(obj, "pretrainedClassification"),
                errorMapper.mapGeneralProcessingErrors(obj));
    }

    private ActionResult<String> mapStringActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var result = obj.getStringOrNull("result");
            var error = errorMapper.mapActionError(obj);
            if (isSuccess) {
                return ActionResult.<String> success(result);
            }
            return ActionResult.<String> failure(error);
        }).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private ActionResult<Map<String, Object>> mapMapActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            Map<String, Object> result = null;
            var resultNode = obj.getProperties().get("result");
            if (resultNode instanceof CICObject resultObj) {
                result = resultObj.toMap();
            }
            if (isSuccess) {
                return ActionResult.<Map<String, Object>> success(result);
            }
            return ActionResult.<Map<String, Object>> failure(error);
        }).orElse(null);
    }

    private ActionResult<List<List<Double>>> mapTextEmbeddingsActionResult(CICObject parent) {
        return parent.getOptionalObject("textEmbeddings").map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            List<List<Double>> result = null;
            var resultNode = obj.getProperties().get("result");
            if (resultNode instanceof CICArray resultArr) {
                if (resultArr.getElements().isEmpty()) {
                    result = List.of();
                } else if (resultArr.getElements().get(0) instanceof CICArray) {
                    var vectors = new ArrayList<List<Double>>();
                    for (var element : resultArr.getElements()) {
                        if (!(element instanceof CICArray vector)) {
                            throw invalidEmbeddingShape("textEmbeddings", "mixed vectors and scalar values");
                        }
                        vectors.add(mapVector(vector, "textEmbeddings"));
                    }
                    result = List.copyOf(vectors);
                } else {
                    result = List.of(mapVector(resultArr, "textEmbeddings"));
                }
            } else if (resultNode != null && !(resultNode instanceof CICPrimitive.CICNull)) {
                throw invalidEmbeddingShape("textEmbeddings", "expected an array");
            }
            if (isSuccess) {
                return ActionResult.<List<List<Double>>> success(result);
            }
            return ActionResult.<List<List<Double>>> failure(error);
        }).orElse(null);
    }

    private ActionResult<List<Double>> mapImageEmbeddingsActionResult(CICObject parent) {
        return parent.getOptionalObject("imageEmbeddings").map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            List<Double> result = null;
            var resultNode = obj.getProperties().get("result");
            if (resultNode instanceof CICArray resultArr) {
                if (resultArr.getElements().size() == 1 && resultArr.getElements().get(0) instanceof CICArray vector) {
                    result = mapVector(vector, "imageEmbeddings");
                } else if (resultArr.getElements().stream().anyMatch(CICArray.class::isInstance)) {
                    throw invalidEmbeddingShape("imageEmbeddings", "expected one vector");
                } else {
                    result = mapVector(resultArr, "imageEmbeddings");
                }
            } else if (resultNode != null && !(resultNode instanceof CICPrimitive.CICNull)) {
                throw invalidEmbeddingShape("imageEmbeddings", "expected an array");
            }
            if (isSuccess) {
                return ActionResult.<List<Double>> success(result);
            }
            return ActionResult.<List<Double>> failure(error);
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

    private ActionResult<Map<String, List<String>>> mapStringListMapActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            Map<String, List<String>> result = null;
            var resultNode = obj.getProperties().get("result");
            if (resultNode instanceof CICObject resultObj) {
                result = new HashMap<>();
                for (var entry : resultObj.getProperties().entrySet()) {
                    if (entry.getValue() instanceof CICArray arr) {
                        result.put(entry.getKey(), arr.toListString());
                    }
                }
            }
            if (isSuccess) {
                return ActionResult.<Map<String, List<String>>> success(result);
            }
            return ActionResult.<Map<String, List<String>>> failure(error);
        }).orElse(null);
    }

    private ActionResult<ClassificationResult> mapClassificationActionResult(CICObject parent, String key) {
        return parent.getOptionalObject(key).map(obj -> {
            var isSuccess = obj.getBoolean("isSuccess", false);
            var error = errorMapper.mapActionError(obj);
            ClassificationResult result = null;
            var resultNode = obj.getProperties().get("result");
            if (resultNode instanceof CICObject resultObj) {
                result = classificationMapper.fromCICObject(resultObj);
            }
            if (isSuccess) {
                return ActionResult.<ClassificationResult> success(result);
            }
            return ActionResult.<ClassificationResult> failure(error);
        }).orElse(null);
    }
}
