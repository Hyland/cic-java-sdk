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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.AccessRight;
import org.hyland.sdk.cic.agent.object.Guardrail;
import org.hyland.sdk.cic.agent.object.GuardrailDefinition;
import org.hyland.sdk.cic.agent.object.GuardrailGroup;
import org.hyland.sdk.cic.agent.object.PrincipalType;
import org.hyland.sdk.cic.agent.object.RagParameters;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
final class AgentMapperUtils {

    private AgentMapperUtils() {
    }

    // -- Deserialization helpers --

    static List<UUID> readUuidList(CICObject cicObject, String key) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey(key)) {
            return null;
        }
        var node = properties.get(key);
        if (node instanceof CICArray array) {
            return array.getElements()
                        .stream()
                        .map(el -> UUID.fromString(
                                ((org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICString) el).getValue()))
                        .collect(Collectors.toList());
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
            return array.toListObject().stream().map(AgentMapperUtils::readAccessRight).collect(Collectors.toList());
        }
        return null;
    }

    static AccessRight readAccessRight(CICObject obj) {
        var type = PrincipalType.fromValue(obj.getStringOrThrow("type"));
        var idStr = obj.getString("id", null);
        var id = idStr != null ? UUID.fromString(idStr) : null;
        return new AccessRight(type, id);
    }

    static List<Guardrail> readGuardrails(CICObject cicObject) {
        var properties = cicObject.getProperties();
        if (!properties.containsKey("guardrails")) {
            return null;
        }
        var node = properties.get("guardrails");
        if (node instanceof CICArray array) {
            return array.toListObject()
                        .stream()
                        .map(obj -> new Guardrail(obj.getStringOrThrow("name")))
                        .collect(Collectors.toList());
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

    static UUID readUuidOrNull(CICObject cicObject, String key) {
        var value = cicObject.getString(key, null);
        return value != null ? UUID.fromString(value) : null;
    }

    static CICNode readNodeOrNull(CICObject cicObject, String key) {
        var properties = cicObject.getProperties();
        var node = properties.get(key);
        if (node instanceof org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICNull) {
            return null;
        }
        return node;
    }

    static Integer getIntegerOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key)
                || props.get(key) instanceof org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICNull) {
            return null;
        }
        return obj.getInt(key, 0);
    }

    static Boolean getBooleanOrNull(CICObject obj, String key) {
        var props = obj.getProperties();
        if (!props.containsKey(key)
                || props.get(key) instanceof org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICNull) {
            return null;
        }
        return obj.getBoolean(key, false);
    }

    // -- Serialization helpers --

    static CICArray writeUuidList(List<UUID> uuids) {
        var array = CICArray.create();
        if (uuids != null) {
            uuids.forEach(uuid -> array.addString(uuid.toString()));
        }
        return array;
    }

    static CICArray writeAccessRights(List<AccessRight> accessRights) {
        var array = CICArray.create();
        if (accessRights != null) {
            accessRights.forEach(ar -> {
                var obj = CICObject.create();
                obj.putString("type", ar.type().value());
                if (ar.id() != null) {
                    obj.putString("id", ar.id().toString());
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

    static List<GuardrailDefinition> readGuardrailDefinitions(CICArray array) {
        return array.toListObject().stream().map(obj -> {
            var name = obj.getStringOrThrow("name");
            var severity = obj.getString("severity", null);
            var isRecommended = obj.getBoolean("isRecommended", false);
            return new GuardrailDefinition(name, severity, isRecommended);
        }).collect(Collectors.toList());
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
        }).collect(Collectors.toList());
    }
}
