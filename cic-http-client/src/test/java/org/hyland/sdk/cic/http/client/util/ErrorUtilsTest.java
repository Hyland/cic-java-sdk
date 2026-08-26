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
package org.hyland.sdk.cic.http.client.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.base.CICHttpResponse;

/**
 * @since 1.0.0
 */
public class ErrorUtilsTest {

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 202, 204, 301, 399 })
    public void throwExceptionOnUnexpectedStatusCode_doesNotThrowForSuccessAndRedirect(int statusCode) {
        var response = responseOf(statusCode, "");
        assertDoesNotThrow(() -> ErrorUtils.throwExceptionOnUnexpectedStatusCode(response));
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 404, 409, 422, 429, 500, 503 })
    public void throwExceptionOnUnexpectedStatusCode_throwsCICServiceExceptionForErrorStatusCodes(int statusCode) {
        var response = responseOf(statusCode, "not-json");
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwExceptionOnUnexpectedStatusCode(response));
        assertEquals(statusCode, exception.statusCode());
    }

    @Test
    public void throwException_alwaysThrowsCICServiceException() {
        var response = responseOf(404, "not-json");
        assertInstanceOf(CICServiceException.class,
                assertThrows(CICServiceException.class, () -> ErrorUtils.throwException(response, "Not found")));
    }

    @Test
    public void throwException_propagatesStatusCode() {
        var response = responseOf(503, "not-json");
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Service unavailable"));
        assertEquals(503, exception.statusCode());
    }

    @Test
    public void throwException_propagatesMessage() {
        var response = responseOf(400, "not-json");
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Bad request"));
        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    public void throwExceptionOnUnexpectedStatusCode_usesStandardMessageWhenBodyCannotBeParsed() {
        var response = responseOf(400, "not-json");

        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwExceptionOnUnexpectedStatusCode(response));

        assertEquals("HTTP response returned with status code: 400", exception.getMessage());
        assertEquals(1, exception.getSuppressed().length);
    }

    @Test
    public void throwException_remoteCauseIsEmptyWhenBodyIsUnparseable() {
        // Without a CICSerializer on the test classpath the MapperService throws CICSdkException,
        // which ErrorUtils catches and proceeds with an empty remoteCause.
        var response = responseOf(400, "not-json");
        var exception = assertThrows(CICServiceException.class,
                () -> ErrorUtils.throwException(response, "Bad request"));
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
