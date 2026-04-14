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
 */
package org.hyland.sdk.cic.http.client.util;

import org.hyland.sdk.cic.http.client.CICError;
import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.base.CICHttpResponse;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
public final class ErrorUtils {

    private ErrorUtils() {
        // utility class
    }

    public static void throwExceptionOnUnexpectedStatusCode(CICHttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (isUnexpectedStatusCode(statusCode)) {
            var exceptionMessage = "HTTP response returned with status code: " + statusCode;
            throwException(response, exceptionMessage);
        }
    }

    public static void throwException(CICHttpResponse<String> response, String exceptionMessage) {
        CICError remoteCause = null;
        try {
            remoteCause = MapperService.read(response.body(), CICError.class);
        } catch (Exception e) {
            // ignore parsing error
        }
        throw new CICServiceException(exceptionMessage, response.statusCode(), remoteCause);
    }

    public static boolean isUnexpectedStatusCode(int statusCode) {
        return statusCode >= 400 && statusCode <= 599;
    }
}
