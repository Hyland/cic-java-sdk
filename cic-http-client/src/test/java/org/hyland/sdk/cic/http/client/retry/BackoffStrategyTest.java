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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class BackoffStrategyTest {

    @Test
    public void fixedDelayReturnsConstantDuration() {
        var strategy = BackoffStrategy.fixedDelay(Duration.ofMillis(500));
        var context = new RetryContext(1, 500, new RuntimeException());
        assertEquals(Duration.ofMillis(500), strategy.computeDelay(context));
        // same delay for subsequent attempts
        var context2 = new RetryContext(5, 500, new RuntimeException());
        assertEquals(Duration.ofMillis(500), strategy.computeDelay(context2));
    }

    @Test
    public void exponentialDelayIsWithinBounds() {
        var strategy = BackoffStrategy.exponentialDelay(Duration.ofMillis(100), Duration.ofSeconds(20));
        // first attempt: delay in [0, 100]
        for (int i = 0; i < 100; i++) {
            var context = new RetryContext(1, 500, new RuntimeException());
            var delay = strategy.computeDelay(context);
            assertTrue(delay.toMillis() >= 0, "Delay should be non-negative");
            assertTrue(delay.toMillis() <= 100,
                    "First attempt delay should be at most 100ms, got: " + delay.toMillis());
        }
    }

    @Test
    public void exponentialDelayGrowsWithAttempts() {
        var strategy = BackoffStrategy.exponentialDelay(Duration.ofMillis(100), Duration.ofSeconds(20));
        // at attempt 5: max delay is min(100 * 2^4, 20000) = 1600ms
        // at attempt 1: max delay is 100ms
        // run enough iterations so the average delay at attempt 5 is reliably > average at attempt 1
        long sumAttempt1 = 0;
        long sumAttempt5 = 0;
        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            sumAttempt1 += strategy.computeDelay(new RetryContext(1, 500, null)).toMillis();
            sumAttempt5 += strategy.computeDelay(new RetryContext(5, 500, null)).toMillis();
        }
        assertTrue(sumAttempt5 > sumAttempt1, "Average delay at attempt 5 should exceed attempt 1");
    }

    @Test
    public void exponentialDelayRespectsCap() {
        var strategy = BackoffStrategy.exponentialDelay(Duration.ofMillis(100), Duration.ofSeconds(1));
        // at attempt 10: exponential = 100 * 2^9 = 51200ms, capped to 1000ms
        for (int i = 0; i < 100; i++) {
            var delay = strategy.computeDelay(new RetryContext(10, 500, null));
            assertTrue(delay.toMillis() <= 1000, "Delay should be capped at maxDelay, got: " + delay.toMillis());
        }
    }

    @Test
    public void exponentialDelayHandlesHighAttemptNumbers() {
        var strategy = BackoffStrategy.exponentialDelay(Duration.ofMillis(100), Duration.ofSeconds(20));
        // attempt 50 should not overflow, capped at shift=30
        var delay = strategy.computeDelay(new RetryContext(50, 500, null));
        assertTrue(delay.toMillis() >= 0, "Delay should be non-negative");
        assertTrue(delay.toMillis() <= 20000, "Delay should be capped at maxDelay");
    }
}
