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
package org.hyland.sdk.cic.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

class CICErrorTest {

    @Test
    void preservesLegacyRecordContract() {
        var first = new CICError("invalid_request", "Bad request body");
        var second = new CICError("invalid_request", "Bad request body");

        assertTrue(CICError.class.isRecord());
        assertEquals("invalid_request", first.error());
        assertEquals("Bad request body", first.errorDescription());
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(new HashSet<>(List.of(first)).contains(second));
        assertEquals("CICError[error=invalid_request, errorDescription=Bad request body]", first.toString());
    }

    @Test
    void canonicalConstructorDefensivelyCopiesExtensions() {
        var extensions = new HashMap<String, Object>();
        extensions.put("model", "model-a");

        var error = new CICError(null, null, "https://example.com/problem", "Problem", 400, "Detail", null, extensions);
        extensions.put("model", "model-b");

        assertEquals("model-a", error.extensions().get("model"));
        assertThrows(UnsupportedOperationException.class, () -> error.extensions().put("category", "category-a"));
    }

    @Test
    void parsesLegacyErrorFields() {
        var error = CICError.from(
                CICObject.from(Map.of("error", "invalid_request", "error_description", "Bad request body")));

        assertFalse(error.isProblemDetail());
        assertEquals("invalid_request", error.error());
        assertEquals("Bad request body", error.errorDescription());
        assertEquals("Bad request body", error.message());
        assertTrue(error.extensions().isEmpty());
    }

    @Test
    void parsesProblemDetailFields() {
        var error = CICError.from(CICObject.from(
                Map.of("type", "https://example.com/problems/forbidden", "title", "Request Processing Error", "status",
                        403, "detail", "Requested model is unavailable.", "instance", "/requests/123")));

        assertTrue(error.isProblemDetail());
        assertEquals("https://example.com/problems/forbidden", error.type());
        assertEquals("Request Processing Error", error.title());
        assertEquals(403, error.status());
        assertEquals("Requested model is unavailable.", error.detail());
        assertEquals("/requests/123", error.instance());
        assertNull(error.error());
        assertNull(error.errorDescription());
    }

    @Test
    @SuppressWarnings("unchecked")
    void capturesExtensionFieldsIncludingNestedValues() {
        var error = CICError.from(CICObject.from(Map.of("title", "Validation failed", "model", "model-a", "errors",
                Map.of("actions.model", List.of("Model is invalid")))));

        assertEquals("model-a", error.extensions().get("model"));
        var errors = (Map<String, Object>) error.extensions().get("errors");
        assertEquals(List.of("Model is invalid"), errors.get("actions.model"));
        assertThrows(UnsupportedOperationException.class, () -> error.extensions().put("anotherExtension", "value"));
    }

    @Test
    void messageUsesExtensionMessageAsFallback() {
        var error = CICError.from(CICObject.from(Map.of("message", "Job not found")));
        assertEquals("Job not found", error.message());
    }

    @SuppressWarnings("unchecked")
    @Test
    void parsesErrorObjectWithoutThrowing() {
        var error = CICError.from(CICObject.from(
                Map.of("error", Map.of("code", "INSUFFICIENT_PERMISSIONS", "message", "Access denied"))));

        assertNull(error.error());
        assertNotNull(error.extensions().get("error"));
        var errorObj = (Map<String, Object>) error.extensions().get("error");
        assertEquals("INSUFFICIENT_PERMISSIONS", errorObj.get("code"));
        assertEquals("Access denied", errorObj.get("message"));
    }

    @Test
    void messageUsesProblemDetailFallbackOrder() {
        assertEquals("Specific detail",
                CICError.from(CICObject.from(Map.of("detail", "Specific detail", "title", "Summary"))).message());
        assertEquals("Summary", CICError.from(CICObject.from(Map.of("title", "Summary"))).message());
        assertEquals("Legacy description",
                CICError.from(CICObject.from(Map.of("error_description", "Legacy description", "error", "legacy")))
                        .message());
        assertEquals("legacy", CICError.from(CICObject.from(Map.of("error", "legacy"))).message());
        assertNull(CICError.from(CICObject.create()).message());
    }
}
