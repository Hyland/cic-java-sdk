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
package org.hyland.sdk.cic.qna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.base.CICHttpResponse;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;

/**
 * Integration test for {@link ErrorUtils#throwException} verifying that a valid {@code CICError} JSON body results in a
 * non-empty {@code remoteCause()} on the thrown {@link CICServiceException}.
 * <p>
 * Requires the Jackson2 serializer on the classpath (provided as a test dependency by {@code cic-http-client-jackson2}
 * in this module's pom).
 *
 * @since 1.0.0
 */
class ErrorUtilsIntegrationTest {

    @Test
    void throwException_remoteCauseIsPresentWhenBodyIsValidCICError() {
        var body = "{\"error\": \"invalid_request\", \"error_description\": \"Bad request body\"}";
        var response = responseOf(400, body);

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Bad request"));

        assertTrue(exception.remoteCause().isPresent());
        assertEquals("invalid_request", exception.remoteCause().get().error());
        assertEquals("Bad request body", exception.remoteCause().get().errorDescription());
    }

    @Test
    void throwException_remoteCauseIsEmptyWhenBodyIsMalformedJson() {
        var response = responseOf(500, "Internal Server Error");

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Server error"));

        assertTrue(exception.remoteCause().isEmpty());
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
