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
 * @since 1.0.0
 */
public final class CICError {

    private static final Set<String> KNOWN_KEYS = Set.of("type", "title", "status", "detail", "instance", "error",
            "error_description");

    private final String type;

    private final String title;

    private final Integer status;

    private final String detail;

    private final String instance;

    private final String error;

    private final String errorDescription;

    private final Map<String, Object> extensions;

    private CICError(String type, String title, Integer status, String detail, String instance, String error,
            String errorDescription, Map<String, Object> extensions) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.error = error;
        this.errorDescription = errorDescription;
        this.extensions = extensions != null ? Collections.unmodifiableMap(extensions) : Map.of();
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

        return new CICError(type, title, status, detail, instance, error, errorDescription,
                extensions.isEmpty() ? null : extensions);
    }

    private static Object toJavaValue(CICNode node) {
        return node.toJavaValue();
    }

    /**
     * RFC 9457 problem type URI identifying the category of problem, or {@code null} for legacy errors.
     */
    public String type() {
        return type;
    }

    /**
     * RFC 9457 short, human-readable summary of the problem type.
     */
    public String title() {
        return title;
    }

    /**
     * RFC 9457 HTTP status code echoed in the response body, or {@code null}.
     */
    public Integer status() {
        return status;
    }

    /**
     * RFC 9457 human-readable explanation specific to this occurrence of the problem.
     */
    public String detail() {
        return detail;
    }

    /**
     * RFC 9457 URI reference identifying the specific occurrence of the problem.
     */
    public String instance() {
        return instance;
    }

    /**
     * Legacy OAuth2-style error code (e.g. "invalid_request"), or {@code null}.
     */
    public String error() {
        return error;
    }

    /**
     * Legacy OAuth2-style error description, or {@code null}.
     */
    public String errorDescription() {
        return errorDescription;
    }

    /**
     * API-specific extension fields not covered by the standard fields (e.g. "model", "category", "errors"). Returns an
     * unmodifiable map; empty if no extensions were present.
     */
    public Map<String, Object> extensions() {
        return extensions;
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
     * {@link #errorDescription()}, then {@link #error()}.
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
        return error;
    }
}
