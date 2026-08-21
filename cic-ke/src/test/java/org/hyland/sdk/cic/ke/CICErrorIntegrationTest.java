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
package org.hyland.sdk.cic.ke;

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
 * Integration tests for {@link org.hyland.sdk.cic.http.client.CICError} verifying that RFC 9457 Problem Details
 * responses are correctly parsed, including extension fields used by the Context API for pretrained classification
 * errors.
 *
 * @since 1.0.0
 */
class CICErrorIntegrationTest {

    @Test
    void legacyErrorFieldsAreParsed() {
        var body = "{\"error\": \"invalid_request\", \"error_description\": \"Bad request body\"}";
        var response = responseOf(400, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        assertTrue(exception.remoteCause().isPresent());
        var error = exception.remoteCause().get();
        assertFalse(error.isProblemDetail());
        assertEquals("invalid_request", error.error());
        assertEquals("Bad request body", error.errorDescription());
        assertEquals("Bad request body", error.message());
    }

    @Test
    void legacyErrorIncludedInExceptionMessage() {
        var body = "{\"error\": \"invalid_request\", \"error_description\": \"Bad request body\"}";
        var response = responseOf(400, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        assertTrue(exception.getMessage().contains("Bad request body"));
    }

    @Test
    void malformedJsonResultsInEmptyRemoteCause() {
        var response = responseOf(500, "Internal Server Error");

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Server error"));

        assertTrue(exception.remoteCause().isEmpty());
        assertEquals("Server error", exception.getMessage());
    }

    // --- RFC 9457 Problem Details: 403 Forbidden (unauthorized model access) ---

    @Test
    void parsesProblemDetails403WithModelAndCategoryExtensions() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.4",
                  "title": "Request Processing Error",
                  "status": 403,
                  "detail": "Requested model 'nbme-organ-system-1' is not available for this caller.",
                  "model": "nbme-organ-system-1",
                  "category": "OrganSystem"
                }
                """;
        var response = responseOf(403, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        assertTrue(exception.remoteCause().isPresent());
        var error = exception.remoteCause().get();

        assertTrue(error.isProblemDetail());
        assertEquals("https://tools.ietf.org/html/rfc9110#section-15.5.4", error.type());
        assertEquals("Request Processing Error", error.title());
        assertEquals(403, error.status());
        assertEquals("Requested model 'nbme-organ-system-1' is not available for this caller.", error.detail());
        assertNull(error.error());
        assertNull(error.errorDescription());

        assertEquals("nbme-organ-system-1", error.extensions().get("model"));
        assertEquals("OrganSystem", error.extensions().get("category"));

        assertEquals("Requested model 'nbme-organ-system-1' is not available for this caller.", error.message());
        assertTrue(exception.getMessage().contains(error.detail()));
    }

    // --- RFC 9457 Problem Details: 400 Bad Request (validation errors) ---

    @Test
    @SuppressWarnings("unchecked")
    void parsesProblemDetails400WithNestedValidationErrors() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.1",
                  "title": "One or more validation errors occurred.",
                  "status": 400,
                  "errors": {
                    "Actions.pretrainedClassification.model": [
                      "Model 'nbme-organ-system-1' is not valid for category 'MediaType'. Allowed models: nbme-media-type."
                    ]
                  }
                }
                """;
        var response = responseOf(400, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        assertTrue(exception.remoteCause().isPresent());
        var error = exception.remoteCause().get();

        assertTrue(error.isProblemDetail());
        assertEquals("https://tools.ietf.org/html/rfc9110#section-15.5.1", error.type());
        assertEquals("One or more validation errors occurred.", error.title());
        assertEquals(400, error.status());
        assertNull(error.detail());

        assertNotNull(error.extensions().get("errors"));
        var errors = (Map<String, Object>) error.extensions().get("errors");
        assertNotNull(errors.get("Actions.pretrainedClassification.model"));
        var modelErrors = (List<Object>) errors.get("Actions.pretrainedClassification.model");
        assertEquals(1, modelErrors.size());
        assertTrue(modelErrors.get(0).toString().contains("nbme-organ-system-1"));

        assertEquals("One or more validation errors occurred.", error.message());
    }

    // --- Edge cases ---

    @Test
    void problemDetailWithNoExtensions() {
        var body = """
                {
                  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
                  "title": "Not Found",
                  "status": 404,
                  "detail": "The requested resource does not exist."
                }
                """;
        var response = responseOf(404, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        var error = exception.remoteCause().get();
        assertTrue(error.isProblemDetail());
        assertTrue(error.extensions().isEmpty());
        assertEquals("The requested resource does not exist.", error.message());
    }

    @Test
    void mixedFormatWithBothLegacyAndProblemDetailFields() {
        var body = """
                {
                  "error": "forbidden",
                  "error_description": "Access denied",
                  "type": "https://example.com/errors/access-denied",
                  "title": "Access Denied",
                  "detail": "You do not have permission to access this resource."
                }
                """;
        var response = responseOf(403, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        var error = exception.remoteCause().get();
        assertTrue(error.isProblemDetail());
        assertEquals("forbidden", error.error());
        assertEquals("Access denied", error.errorDescription());
        assertEquals("You do not have permission to access this resource.", error.detail());
        assertEquals("You do not have permission to access this resource.", error.message());
    }

    @Test
    void nonProblemDetailWithOnlyLegacyErrorCode() {
        var body = "{\"error\": \"server_error\"}";
        var response = responseOf(500, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "HTTP error"));

        var error = exception.remoteCause().get();
        assertFalse(error.isProblemDetail());
        assertEquals("server_error", error.error());
        assertEquals("server_error", error.message());
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
