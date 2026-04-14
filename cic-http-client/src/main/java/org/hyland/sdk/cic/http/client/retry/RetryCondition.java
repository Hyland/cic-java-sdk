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

import java.io.IOException;
import java.util.Set;

/**
 * Determines whether a failed HTTP request should be retried.
 * <p>
 * The {@link #shouldRetry(RetryContext)} method receives full context about the failure, including the HTTP status
 * code, the exception, and the attempt number, allowing implementations to make fine-grained retry decisions.
 * <p>
 * Conditions can be composed using {@link #and(RetryCondition)} and {@link #or(RetryCondition)}.
 * <p>
 * Built-in conditions:
 * <ul>
 * <li>{@link #defaultCondition()} - retries on server errors (500, 502, 503, 504), rate limiting (429), request timeout (408) and
 * {@link IOException}</li>
 * <li>{@link #none()} - never retries</li>
 * </ul>
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface RetryCondition {

    /**
     * HTTP status codes that are retried by the {@link #defaultCondition()}.
     */
    Set<Integer> DEFAULT_RETRYABLE_STATUS_CODES = Set.of(408, 429, 500, 502, 503, 504);

    /**
     * Determines whether the request should be retried based on the given context.
     *
     * @param context the retry context describing the failed attempt
     * @return {@code true} if the request should be retried, {@code false} otherwise
     */
    boolean shouldRetry(RetryContext context);

    /**
     * Returns a condition that retries only when both this condition and the other condition agree.
     *
     * @param other the other condition
     * @return a combined condition using logical AND
     */
    default RetryCondition and(RetryCondition other) {
        return context -> this.shouldRetry(context) && other.shouldRetry(context);
    }

    /**
     * Returns a condition that retries when either this condition or the other condition agrees.
     *
     * @param other the other condition
     * @return a combined condition using logical OR
     */
    default RetryCondition or(RetryCondition other) {
        return context -> this.shouldRetry(context) || other.shouldRetry(context);
    }

    /**
     * Returns the default retry condition.
     * <p>
     * Retries when the HTTP status code is one of 429 (Too Many Requests), 500 (Internal Server Error), 502 (Bad
     * Gateway), 503 (Service Unavailable), or 504 (Gateway Timeout), or when the exception is or is caused by an
     * {@link IOException}.
     *
     * @return the default retry condition
     */
    static RetryCondition defaultCondition() {
        return context -> {
            if (DEFAULT_RETRYABLE_STATUS_CODES.contains(context.statusCode())) {
                return true;
            }
            // check if exception chain contains an IOException
            Throwable cause = context.exception();
            while (cause != null) {
                if (cause instanceof IOException) {
                    return true;
                }
                cause = cause.getCause();
            }
            return false;
        };
    }

    /**
     * Returns a condition that never retries.
     *
     * @return a condition that always returns {@code false}
     */
    static RetryCondition none() {
        return context -> false;
    }
}
