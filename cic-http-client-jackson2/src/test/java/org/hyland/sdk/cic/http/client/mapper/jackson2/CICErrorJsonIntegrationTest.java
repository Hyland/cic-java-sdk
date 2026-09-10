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
package org.hyland.sdk.cic.http.client.mapper.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.base.CICHttpResponse;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;

/**
 * Integration tests for RFC 9457 and legacy error JSON deserialization through the Jackson serializer.
 *
 * @since 1.0.0
 */
class CICErrorJsonIntegrationTest {

    @Test
    void parsesLegacyErrorJson() {
        var response = responseOf(400, "{\"error\": \"invalid_request\", \"error_description\": \"Bad request body\"}");

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        assertTrue(exception.remoteCause().isPresent());
        var error = exception.remoteCause().get();
        assertFalse(error.isProblemDetail());
        assertEquals("invalid_request", error.error());
        assertEquals("Bad request body", error.errorDescription());
        assertEquals("Bad request body", error.message());
        assertEquals("HTTP error", exception.getMessage());
    }

    @Test
    void parsesProblemDetailsWithExtensionFields() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.4",
                  "title": "Request Processing Error",
                  "status": 403,
                  "detail": "Requested model 'pretrained-model-b' is not available for this caller.",
                  "model": "pretrained-model-b",
                  "category": "OrganSystem"
                }
                """;

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(403, body), "HTTP error"));

        var error = exception.remoteCause().orElseThrow();
        assertTrue(error.isProblemDetail());
        assertEquals("https://tools.ietf.org/html/rfc9110#section-15.5.4", error.type());
        assertEquals("Request Processing Error", error.title());
        assertEquals(403, error.status());
        assertEquals("Requested model 'pretrained-model-b' is not available for this caller.", error.detail());
        assertNull(error.error());
        assertNull(error.errorDescription());
        assertEquals("pretrained-model-b", error.extensions().get("model"));
        assertEquals("OrganSystem", error.extensions().get("category"));
        assertEquals("HTTP error", exception.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    void parsesNestedValidationErrorExtensions() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.1",
                  "title": "One or more validation errors occurred.",
                  "status": 400,
                  "errors": {
                    "Actions.pretrainedClassification.model": [
                      "Model 'pretrained-model-b' is not valid for category 'MediaType'."
                    ]
                  }
                }
                """;

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(400, body), "HTTP error"));

        var error = exception.remoteCause().orElseThrow();
        assertNotNull(error.extensions().get("errors"));
        var errors = (Map<String, Object>) error.extensions().get("errors");
        var modelErrors = (List<Object>) errors.get("Actions.pretrainedClassification.model");
        assertEquals(1, modelErrors.size());
        assertTrue(modelErrors.get(0).toString().contains("pretrained-model-b"));
        assertEquals("One or more validation errors occurred.", error.message());
    }

    @Test
    void parsesProblemDetailWithoutExtensions() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
                  "title": "Not Found",
                  "status": 404,
                  "detail": "The requested resource does not exist."
                }
                """;

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(404, body), "HTTP error"));

        var error = exception.remoteCause().orElseThrow();
        assertTrue(error.isProblemDetail());
        assertTrue(error.extensions().isEmpty());
        assertEquals("The requested resource does not exist.", error.message());
    }

    @Test
    void parsesMixedLegacyAndProblemDetailFields() {
        var body = """
                {
                  "error": "forbidden",
                  "error_description": "Access denied",
                  "type": "https://example.com/errors/access-denied",
                  "title": "Access Denied",
                  "detail": "You do not have permission to access this resource."
                }
                """;

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(403, body), "HTTP error"));

        var error = exception.remoteCause().orElseThrow();
        assertTrue(error.isProblemDetail());
        assertEquals("forbidden", error.error());
        assertEquals("Access denied", error.errorDescription());
        assertEquals("You do not have permission to access this resource.", error.detail());
        assertEquals("You do not have permission to access this resource.", error.message());
    }

    @Test
    void parsesLegacyErrorCodeWithoutDescription() {
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(500, "{\"error\": \"server_error\"}"), "HTTP error"));

        var error = exception.remoteCause().orElseThrow();
        assertFalse(error.isProblemDetail());
        assertEquals("server_error", error.error());
        assertEquals("server_error", error.message());
    }

    @Test
    void parsesDataCurationMessageOnlyError() {
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(responseOf(404, "{\"message\": \"Job not found\"}"), "HTTP error"));

        assertTrue(exception.remoteCause().isPresent());
        assertEquals("Job not found", exception.remoteCause().get().message());
        assertEquals("HTTP error", exception.getMessage());
    }

    private static CICHttpResponse<String> responseOf(int statusCode, String body) {
        return new CICHttpResponse<>() {
            @Override
            public String body() {
                return body;
            }

            @Override
            public int statusCode() {
                return statusCode;
            }
        };
    }
}
