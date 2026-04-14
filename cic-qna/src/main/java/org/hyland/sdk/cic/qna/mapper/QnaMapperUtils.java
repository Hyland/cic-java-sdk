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
package org.hyland.sdk.cic.qna.mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.http.client.pagination.Pagination;
import org.hyland.sdk.cic.qna.object.AnswerObjectReferences;
import org.hyland.sdk.cic.qna.object.AnswerReferenceItem;
import org.hyland.sdk.cic.qna.object.DocumentReferenceItem;
import org.hyland.sdk.cic.qna.object.DocumentReferences;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.FilterExpression;
import org.hyland.sdk.cic.qna.object.MessageStatus;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;
import org.hyland.sdk.cic.qna.object.SetAnswerReference;

/**
 * @since 1.0.0
 */
final class QnaMapperUtils {

    private QnaMapperUtils() {
    }

    static String readStringOrNull(CICObject obj, String key) {
        return obj.getString(key, null);
    }

    static Integer getIntegerOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key) || props.get(key) instanceof CICPrimitive.CICNull) {
            return null;
        }
        return obj.getInt(key, 0);
    }

    static Double getDoubleOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key) || props.get(key) instanceof CICPrimitive.CICNull) {
            return null;
        }
        return obj.getDouble(key, 0.0);
    }

    static FilterExpression readFilterExpressionOrNull(CICObject cicObject, String key) {
        var properties = cicObject.getProperties();
        var node = properties.get(key);
        if (node == null || node instanceof CICPrimitive.CICNull) {
            return null;
        }
        if (!(node instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject for filter expression at key '" + key + "', got: "
                    + node.getClass().getSimpleName());
        }
        return FilterExpression.of(toMap(obj));
    }

    static FeedbackType readFeedbackTypeOrNull(CICObject obj, String key) {
        var value = obj.getString(key, null);
        return value != null ? FeedbackType.fromValue(value) : null;
    }

    static ResponseCompleteness readResponseCompletenessOrNull(CICObject obj, String key) {
        var value = obj.getString(key, null);
        return value != null ? ResponseCompleteness.fromValue(value) : null;
    }

    static MessageStatus readMessageStatus(CICObject obj, String key) {
        return MessageStatus.fromValue(obj.getStringOrThrow(key));
    }

    static List<String> readStringList(CICObject cicObject, String key) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey(key)) {
            return null;
        }
        var node = properties.get(key);
        if (node instanceof CICArray array) {
            return array.getElements().stream().map(el -> {
                if (el instanceof CICPrimitive.CICString s) {
                    return s.getValue();
                }
                throw new IllegalStateException(
                        "Expected CICString in string list, got: " + el.getClass().getSimpleName());
            }).toList();
        }
        throw new IllegalArgumentException(
                "Expected CICArray for key '" + key + "', got: " + node.getClass().getSimpleName());
    }

    static CursorPagination readCursorPagination(CICObject obj) {
        var paginationNode = obj.getProperties().get("pagination");
        if (!(paginationNode instanceof CICObject paginationObj)) {
            throw new IllegalArgumentException("Expected CICObject for pagination");
        }
        var nextCursor = paginationObj.getString("nextCursor", null);
        var hasMore = paginationObj.getBoolean("hasMore", false);
        return new CursorPagination(nextCursor, hasMore);
    }

    static Pagination readPagination(CICObject obj) {
        var paginationNode = obj.getProperties().get("pagination");
        if (!(paginationNode instanceof CICObject paginationObj)) {
            throw new IllegalArgumentException("Expected CICObject for pagination");
        }
        return new Pagination(paginationObj.getInt("pageSize", 0), paginationObj.getInt("pageNumber", 0),
                paginationObj.getInt("totalItems", 0), paginationObj.getInt("totalPages", 0));
    }

    static List<DocumentReferences> readDocumentReferences(CICObject obj) {
        return readDocumentReferences(obj, "documentReferences");
    }

    static List<DocumentReferences> readDocumentReferences(CICObject obj, String key) {
        var node = obj.getProperties().get(key);
        if (node == null || node instanceof CICPrimitive.CICNull) {
            return null;
        }
        if (!(node instanceof CICArray array)) {
            throw new IllegalArgumentException(
                    "Expected CICArray for key '" + key + "', got: " + node.getClass().getSimpleName());
        }
        return array.toListObject().stream().map(docObj -> {
            var documentId = docObj.getString("documentId", null);
            var refsNode = docObj.getProperties().get("references");
            List<DocumentReferenceItem> refs = List.of();
            if (refsNode instanceof CICArray refsArray) {
                refs = refsArray.toListObject()
                                .stream()
                                .map(refObj -> new DocumentReferenceItem(refObj.getStringOrThrow("referenceId"),
                                        refObj.getDouble("rankScore", 0.0), getIntegerOrNull(refObj, "rank")))
                                .toList();
            }
            return new DocumentReferences(documentId, refs);
        }).toList();
    }

    static List<AnswerObjectReferences> readAnswerObjectReferences(CICObject obj) {
        var node = obj.getProperties().get("objectReferences");
        if (node == null || node instanceof CICPrimitive.CICNull) {
            return null;
        }
        if (!(node instanceof CICArray array)) {
            throw new IllegalArgumentException(
                    "Expected CICArray for key 'objectReferences', got: " + node.getClass().getSimpleName());
        }
        return array.toListObject().stream().map(refObj -> {
            var objectId = refObj.getString("objectId", null);
            var refsNode = refObj.getProperties().get("references");
            List<AnswerReferenceItem> refs = List.of();
            if (refsNode instanceof CICArray refsArray) {
                refs = refsArray.toListObject()
                                .stream()
                                .map(r -> new AnswerReferenceItem(r.getStringOrThrow("referenceId"),
                                        r.getDouble("rankScore", 0.0), getIntegerOrNull(r, "rank")))
                                .toList();
            }
            return new AnswerObjectReferences(objectId, refs);
        }).toList();
    }

    static CICArray writeStringList(List<String> strings) {
        var array = CICArray.create();
        if (strings != null) {
            strings.forEach(array::addString);
        }
        return array;
    }

    static CICObject toCICObject(FilterExpression expression) {
        var obj = CICObject.create();
        expression.properties().forEach((key, value) -> putValue(obj, key, value));
        return obj;
    }

    static CICArray writeSetAnswerReferences(List<SetAnswerReference> references) {
        var array = CICArray.create();
        if (references != null) {
            references.forEach(ref -> {
                var obj = CICObject.create();
                obj.putString("referenceId", ref.referenceId());
                obj.putString("objectId", ref.objectId());
                obj.putDouble("rankScore", ref.rankScore());
                array.addObject(obj);
            });
        }
        return array;
    }

    static Map<String, Object> toMap(CICObject obj) {
        var map = new LinkedHashMap<String, Object>();
        obj.getProperties().forEach((key, node) -> map.put(key, toJavaValue(node)));
        return map;
    }

    private static List<Object> toList(CICArray array) {
        return array.getElements().stream().map(QnaMapperUtils::toJavaValue).toList();
    }

    private static Object toJavaValue(CICNode node) {
        if (node instanceof CICPrimitive.CICString s) {
            return s.getValue();
        } else if (node instanceof CICPrimitive.CICInt i) {
            return i.getValue();
        } else if (node instanceof CICPrimitive.CICLong l) {
            return l.getValue();
        } else if (node instanceof CICPrimitive.CICDouble d) {
            return d.getValue();
        } else if (node instanceof CICPrimitive.CICBoolean b) {
            return b.getValue();
        } else if (node instanceof CICPrimitive.CICNull) {
            return null;
        } else if (node instanceof CICObject obj) {
            return toMap(obj);
        } else if (node instanceof CICArray arr) {
            return toList(arr);
        }
        throw new IllegalStateException("Unsupported CICNode type: " + node.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private static void putValue(CICObject target, String key, Object value) {
        if (value == null) {
            target.putNull(key);
        } else if (value instanceof String s) {
            target.putString(key, s);
        } else if (value instanceof Integer i) {
            target.putInt(key, i);
        } else if (value instanceof Long l) {
            target.putLong(key, l);
        } else if (value instanceof Double d) {
            target.putDouble(key, d);
        } else if (value instanceof Boolean b) {
            target.putBoolean(key, b);
        } else if (value instanceof List<?> list) {
            target.putArray(key, toArray(list));
        } else if (value instanceof Map<?, ?> map) {
            target.putObject(key, toCICObject(FilterExpression.of((Map<String, Object>) map)));
        } else {
            throw new IllegalArgumentException(
                    "Unsupported value type for key '" + key + "': " + value.getClass().getName());
        }
    }

    @SuppressWarnings("unchecked")
    private static CICArray toArray(List<?> elements) {
        var array = CICArray.create();
        for (var el : elements) {
            if (el instanceof String s) {
                array.addString(s);
            } else if (el instanceof Integer i) {
                array.addInt(i);
            } else if (el instanceof Long l) {
                array.addLong(l);
            } else if (el instanceof Double d) {
                array.addDouble(d);
            } else if (el instanceof Boolean b) {
                array.addBoolean(b);
            } else if (el instanceof List<?> list) {
                array.addArray(toArray(list));
            } else if (el instanceof Map<?, ?> map) {
                array.addObject(toCICObject(FilterExpression.of((Map<String, Object>) map)));
            } else {
                throw new IllegalArgumentException(
                        "Unsupported array element type: " + (el == null ? "null" : el.getClass().getName()));
            }
        }
        return array;
    }
}
