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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.http.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * Represents an error response from the CIC API, supporting both legacy OAuth2-style error responses and RFC 9457
 * Problem Details format.
 * <p>
 * Legacy format fields: {@link #error()}, {@link #errorDescription()}
 * <p>
 * RFC 9457 Problem Details fields: {@link #type()}, {@link #title()}, {@link #status()}, {@link #detail()}
 * <p>
 * Any additional API-specific fields (e.g. "model", "category", "errors") are captured in {@link #extensions()}.
 *
 * @param error legacy OAuth2-style error code
 * @param errorDescription legacy OAuth2-style error description
 * @param type RFC 9457 problem type URI
 * @param title RFC 9457 short, human-readable problem summary
 * @param status RFC 9457 HTTP status code
 * @param detail RFC 9457 explanation specific to this problem occurrence
 * @param instance RFC 9457 URI identifying this problem occurrence
 * @param extensions API-specific fields not covered by the standard fields
 * @since 1.0.0
 */
public record CICError(String error, String errorDescription, String type, String title, Integer status, String detail,
        String instance, Map<String, Object> extensions) {

    private static final Set<String> KNOWN_KEYS = Set.of("type", "title", "status", "detail", "instance", "error",
            "error_description");

    public CICError {
        extensions = extensions == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(extensions));
    }

    /**
     * Creates a legacy OAuth2-style error, preserving the constructor available in SDK 1.0.0.
     *
     * @param error the legacy error code
     * @param errorDescription the legacy error description
     */
    public CICError(String error, String errorDescription) {
        this(error, errorDescription, null, null, null, null, null, Map.of());
    }

    /**
     * Parses a {@link CICError} from a {@link CICObject}, reading both legacy and RFC 9457 fields. Any unrecognized
     * fields are placed into the {@link #extensions()} map.
     */
    public static CICError from(CICObject cicObject) {
        String type = cicObject.getString("type", null);
        String title = cicObject.getString("title", null);
        Integer status = cicObject.getIntegerOrNull("status");
        String detail = cicObject.getString("detail", null);
        String instance = cicObject.getString("instance", null);
        String error = cicObject.getString("error", null);
        String errorDescription = cicObject.getString("error_description", null);

        Map<String, Object> extensions = new LinkedHashMap<>();
        for (var entry : cicObject.getProperties().entrySet()) {
            if (!KNOWN_KEYS.contains(entry.getKey())) {
                extensions.put(entry.getKey(), toJavaValue(entry.getValue()));
            }
        }

        return new CICError(error, errorDescription, type, title, status, detail, instance,
                extensions.isEmpty() ? null : extensions);
    }

    private static Object toJavaValue(CICNode node) {
        return node.toJavaValue();
    }

    /**
     * Returns {@code true} if this error uses the RFC 9457 Problem Details format (has a {@code type} or
     * {@code title}).
     */
    public boolean isProblemDetail() {
        return type != null || title != null;
    }

    /**
     * Best-effort human-readable message: prefers {@link #detail()}, falls back to {@link #title()}, then
     * {@link #errorDescription()}, then {@link #error()}, and finally checks for a {@code "message"} extension field
     * (used by the Data Curation API).
     */
    public String message() {
        if (detail != null) {
            return detail;
        }
        if (title != null) {
            return title;
        }
        if (errorDescription != null) {
            return errorDescription;
        }
        if (error != null) {
            return error;
        }
        var extensionMessage = extensions.get("message");
        if (extensionMessage instanceof String msg) {
            return msg;
        }
        return null;
    }

    /**
     * Preserves the SDK 1.0.0 representation for legacy errors while including all fields for RFC 9457 errors.
     */
    @Override
    public String toString() {
        if (type == null && title == null && status == null && detail == null && instance == null
                && extensions.isEmpty()) {
            return "CICError[error=" + error + ", errorDescription=" + errorDescription + "]";
        }
        return "CICError[error=" + error + ", errorDescription=" + errorDescription + ", type=" + type + ", title="
                + title + ", status=" + status + ", detail=" + detail + ", instance=" + instance + ", extensions="
                + extensions + "]";
    }
}
