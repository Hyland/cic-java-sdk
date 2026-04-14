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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class RetryPolicyTest {

    @Test
    public void defaultPolicy() {
        var policy = RetryPolicy.defaultPolicy();
        assertEquals(RetryPolicy.DEFAULT_MAX_ATTEMPTS, policy.maxAttempts());
        assertNotNull(policy.backoffStrategy());
        assertNotNull(policy.retryCondition());
    }

    @Test
    public void nonePolicy() {
        var policy = RetryPolicy.none();
        assertEquals(1, policy.maxAttempts());
        assertNotNull(policy.backoffStrategy());
        // none policy should never retry
        var context = new RetryContext(1, 500, new RuntimeException());
        assertFalse(policy.retryCondition().shouldRetry(context));
    }

    @Test
    public void builderWithCustomValues() {
        var policy = RetryPolicy.builder()
                                .maxAttempts(5)
                                .backoffStrategy(BackoffStrategy.fixedDelay(java.time.Duration.ofSeconds(1)))
                                .retryCondition(ctx -> ctx.statusCode() == 503)
                                .build();
        assertEquals(5, policy.maxAttempts());
        assertNotNull(policy.backoffStrategy());
        assertNotNull(policy.retryCondition());
    }

    @Test
    public void builderRejectsZeroMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.builder().maxAttempts(0));
    }

    @Test
    public void builderRejectsNegativeMaxAttempts() {
        assertThrows(IllegalArgumentException.class, () -> RetryPolicy.builder().maxAttempts(-1));
    }

    @Test
    public void builderAcceptsOneMaxAttempt() {
        var policy = RetryPolicy.builder().maxAttempts(1).build();
        assertEquals(1, policy.maxAttempts());
    }
}
