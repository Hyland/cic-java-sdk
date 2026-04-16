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
 *     Damian Ujma
 */
package org.hyland.sdk.cic.http.client.retry;

/**
 * Provides context about a failed HTTP request attempt to {@link RetryCondition} and {@link BackoffStrategy}.
 *
 * @param attemptNumber the 1-based attempt number that just failed
 * @param httpMethod the HTTP method of the request (e.g. {@code "GET"}, {@code "POST"})
 * @param statusCode the HTTP status code returned, or {@code 0} if the failure occurred before a response was received
 * @param exception the exception that caused the failure
 * @since 1.0.0
 */
public record RetryContext(int attemptNumber, String httpMethod, int statusCode, Exception exception) {

}
