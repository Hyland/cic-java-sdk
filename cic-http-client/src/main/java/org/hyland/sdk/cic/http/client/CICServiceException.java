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
package org.hyland.sdk.cic.http.client;

import java.util.Optional;

/**
 * Exception thrown when the CIC service responds with an error HTTP status code (4xx or 5xx).
 * <p>
 * Carries the HTTP {@link #statusCode()} and, when the response body could be parsed, a structured
 * {@link #remoteCause()} describing the error reported by the service.
 *
 * @since 1.0.0
 */
public class CICServiceException extends CICSdkException {

    private final int statusCode;

    private final CICError remoteCause;

    public CICServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.remoteCause = null;
    }

    public CICServiceException(String message, int statusCode, CICError remoteCause) {
        super(message);
        this.statusCode = statusCode;
        this.remoteCause = remoteCause;
    }

    /**
     * @return the HTTP status code returned by the service
     */
    public int statusCode() {
        return statusCode;
    }

    /**
     * @return the structured error parsed from the response body, or an empty {@link Optional} if the body could not be
     *         parsed as a {@link CICError}
     */
    public Optional<CICError> remoteCause() {
        return Optional.ofNullable(remoteCause);
    }
}
