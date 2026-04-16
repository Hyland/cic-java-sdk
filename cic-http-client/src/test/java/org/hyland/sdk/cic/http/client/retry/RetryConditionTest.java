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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @since 1.0.0
 */
public class RetryConditionTest {

    @ParameterizedTest
    @ValueSource(ints = { 408, 429, 500, 502, 503, 504 })
    public void defaultConditionRetriesOnRetryableStatusCodes(int statusCode) {
        var condition = RetryCondition.defaultCondition();
        assertTrue(condition.shouldRetry(new RetryContext(1, "GET", statusCode, null)));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 400, 401, 403, 404, 409, 422 })
    public void defaultConditionDoesNotRetryOnNonRetryableStatusCodes(int statusCode) {
        var condition = RetryCondition.defaultCondition();
        assertFalse(condition.shouldRetry(new RetryContext(1, "GET", statusCode, null)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "GET", "HEAD", "PUT", "DELETE", "OPTIONS", "TRACE" })
    public void defaultConditionRetriesIdempotentMethodsOnIOException(String method) {
        var condition = RetryCondition.defaultCondition();
        assertTrue(condition.shouldRetry(new RetryContext(1, method, 0, new IOException("Connection reset"))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PATCH" })
    public void defaultConditionDoesNotRetryNonIdempotentMethodsOnIOException(String method) {
        var condition = RetryCondition.defaultCondition();
        assertFalse(condition.shouldRetry(new RetryContext(1, method, 0, new IOException("Connection reset"))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "POST", "PATCH" })
    public void defaultConditionDoesNotRetryNonIdempotentMethodsOnRetryableStatusCode(String method) {
        var condition = RetryCondition.defaultCondition();
        assertFalse(condition.shouldRetry(new RetryContext(1, method, 500, null)));
        assertFalse(condition.shouldRetry(new RetryContext(1, method, 429, null)));
        assertFalse(condition.shouldRetry(new RetryContext(1, method, 503, null)));
    }

    @Test
    public void defaultConditionRetriesOnWrappedIOException() {
        var condition = RetryCondition.defaultCondition();
        var wrappedException = new RuntimeException("wrapper", new IOException("Connection reset"));
        assertTrue(condition.shouldRetry(new RetryContext(1, "GET", 0, wrappedException)));
    }

    @Test
    public void defaultConditionRetriesOnUncheckedIOException() {
        var condition = RetryCondition.defaultCondition();
        var exception = new UncheckedIOException(new IOException("Connection reset"));
        assertTrue(condition.shouldRetry(new RetryContext(1, "GET", 0, exception)));
    }

    @Test
    public void noneConditionNeverRetries() {
        var condition = RetryCondition.none();
        assertFalse(condition.shouldRetry(new RetryContext(1, "GET", 500, null)));
        assertFalse(condition.shouldRetry(new RetryContext(1, "GET", 0, new IOException())));
    }

    @Test
    public void andCombinator() {
        RetryCondition statusIs500 = ctx -> ctx.statusCode() == 500;
        RetryCondition attemptIsFirst = ctx -> ctx.attemptNumber() == 1;
        var combined = statusIs500.and(attemptIsFirst);
        // both true
        assertTrue(combined.shouldRetry(new RetryContext(1, "GET", 500, null)));
        // status doesn't match
        assertFalse(combined.shouldRetry(new RetryContext(1, "GET", 503, null)));
        // attempt doesn't match
        assertFalse(combined.shouldRetry(new RetryContext(2, "GET", 500, null)));
    }

    @Test
    public void orCombinator() {
        RetryCondition statusIs500 = ctx -> ctx.statusCode() == 500;
        RetryCondition statusIs503 = ctx -> ctx.statusCode() == 503;
        var combined = statusIs500.or(statusIs503);
        assertTrue(combined.shouldRetry(new RetryContext(1, "GET", 500, null)));
        assertTrue(combined.shouldRetry(new RetryContext(1, "GET", 503, null)));
        assertFalse(combined.shouldRetry(new RetryContext(1, "GET", 404, null)));
    }
}
