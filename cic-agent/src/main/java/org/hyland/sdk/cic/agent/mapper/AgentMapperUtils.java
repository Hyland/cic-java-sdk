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
package org.hyland.sdk.cic.agent.mapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.agent.object.AccessRight;
import org.hyland.sdk.cic.agent.object.FilterExpression;
import org.hyland.sdk.cic.agent.object.Guardrail;
import org.hyland.sdk.cic.agent.object.GuardrailDefinition;
import org.hyland.sdk.cic.agent.object.GuardrailGroup;
import org.hyland.sdk.cic.agent.object.PrincipalType;
import org.hyland.sdk.cic.agent.object.RagParameters;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;

/**
 * @since 1.0.0
 */
final class AgentMapperUtils {

    private AgentMapperUtils() {
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
        return null;
    }

    static List<AccessRight> readAccessRights(CICObject cicObject) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey("accessRights")) {
            return null;
        }
        var node = properties.get("accessRights");
        if (node instanceof CICArray array) {
            return array.toListObject().stream().map(AgentMapperUtils::readAccessRight).toList();
        }
        return null;
    }

    static AccessRight readAccessRight(CICObject obj) {
        var type = PrincipalType.fromValue(obj.getStringOrThrow("type"));
        var id = obj.getString("id", null);
        return new AccessRight(type, id);
    }

    static List<Guardrail> readGuardrails(CICObject cicObject) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey("guardrails")) {
            return null;
        }
        var node = properties.get("guardrails");
        if (node instanceof CICArray array) {
            return array.toListObject().stream().map(obj -> new Guardrail(obj.getStringOrThrow("name"))).toList();
        }
        return null;
    }

    static RagParameters readRagParameters(CICObject cicObject) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey("ragParameters")) {
            return null;
        }
        var node = properties.get("ragParameters");
        if (node instanceof CICObject ragObj) {
            return new RagParameters(getIntegerOrNull(ragObj, "limit"), getIntegerOrNull(ragObj, "adjacentChunkRange"),
                    getBooleanOrNull(ragObj, "adjacentChunkMerge"), getBooleanOrNull(ragObj, "rerankerEnabled"),
                    getIntegerOrNull(ragObj, "rerankerTopN"));
        }
        return null;
    }

    static String readStringOrNull(CICObject cicObject, String key) {
        return cicObject.getString(key, null);
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

    static Integer getIntegerOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key) || props.get(key) instanceof CICPrimitive.CICNull) {
            return null;
        }
        return obj.getInt(key, 0);
    }

    static Boolean getBooleanOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key) || props.get(key) instanceof CICPrimitive.CICNull) {
            return null;
        }
        return obj.getBoolean(key, false);
    }

    static CICArray writeAccessRights(List<AccessRight> accessRights) {
        var array = CICArray.create();
        if (accessRights != null) {
            accessRights.forEach(ar -> {
                var obj = CICObject.create();
                obj.putString("type", ar.type().value());
                if (ar.id() != null) {
                    obj.putString("id", ar.id());
                }
                array.addObject(obj);
            });
        }
        return array;
    }

    static CICArray writeGuardrails(List<Guardrail> guardrails) {
        var array = CICArray.create();
        if (guardrails != null) {
            guardrails.forEach(g -> {
                var obj = CICObject.create();
                obj.putString("name", g.name());
                array.addObject(obj);
            });
        }
        return array;
    }

    static CICObject writeRagParameters(RagParameters ragParameters) {
        var obj = CICObject.create();
        if (ragParameters.limit() != null) {
            obj.putInt("limit", ragParameters.limit());
        }
        if (ragParameters.adjacentChunkRange() != null) {
            obj.putInt("adjacentChunkRange", ragParameters.adjacentChunkRange());
        }
        if (ragParameters.adjacentChunkMerge() != null) {
            obj.putBoolean("adjacentChunkMerge", ragParameters.adjacentChunkMerge());
        }
        if (ragParameters.rerankerEnabled() != null) {
            obj.putBoolean("rerankerEnabled", ragParameters.rerankerEnabled());
        }
        if (ragParameters.rerankerTopN() != null) {
            obj.putInt("rerankerTopN", ragParameters.rerankerTopN());
        }
        return obj;
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

    static List<GuardrailDefinition> readGuardrailDefinitions(CICArray array) {
        return array.toListObject().stream().map(obj -> {
            var name = obj.getStringOrThrow("name");
            var severity = obj.getString("severity", null);
            var isRecommended = obj.getBoolean("isRecommended", false);
            return new GuardrailDefinition(name, severity, isRecommended);
        }).toList();
    }

    static List<GuardrailGroup> readGuardrailGroups(CICArray array) {
        return array.toListObject().stream().map(obj -> {
            var displayName = obj.getStringOrThrow("displayName");
            var description = obj.getStringOrThrow("description");
            var guardrailsNode = obj.getProperties().get("guardrails");
            List<GuardrailDefinition> guardrails = null;
            if (guardrailsNode instanceof CICArray guardrailsArray) {
                guardrails = readGuardrailDefinitions(guardrailsArray);
            }
            return new GuardrailGroup(displayName, description, guardrails);
        }).toList();
    }

    private static Map<String, Object> toMap(CICObject obj) {
        var map = new LinkedHashMap<String, Object>();
        obj.getProperties().forEach((key, node) -> map.put(key, toJavaValue(node)));
        return map;
    }

    private static List<Object> toList(CICArray array) {
        return array.getElements().stream().map(AgentMapperUtils::toJavaValue).toList();
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
