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

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Strategy for computing the delay between retry attempts.
 * <p>
 * Implementations determine how long to wait before the next retry based on the {@link RetryContext}, which includes
 * the attempt number, HTTP status code, and the exception that triggered the retry.
 * <p>
 * Built-in strategies are available via static factory methods:
 * <ul>
 * <li>{@link #fixedDelay(Duration)} - constant delay between retries</li>
 * <li>{@link #exponentialDelay(Duration, Duration)} - exponential backoff with full jitter</li>
 * </ul>
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface BackoffStrategy {

    /**
     * Computes the delay to wait before the next retry attempt.
     *
     * @param context the retry context describing the failed attempt
     * @return the duration to wait before retrying; must not be negative
     */
    Duration computeDelay(RetryContext context);

    /**
     * Creates a strategy that waits a fixed duration between retries.
     *
     * @param delay the constant delay between retries
     * @return a fixed-delay backoff strategy
     * @throws IllegalArgumentException if {@code delay} is negative
     */
    static BackoffStrategy fixedDelay(Duration delay) {
        Objects.requireNonNull(delay, "delay must not be null");
        if (delay.isNegative()) {
            throw new IllegalArgumentException("delay must not be negative, got: " + delay);
        }
        return context -> delay;
    }

    /**
     * Creates a strategy using exponential backoff with full jitter.
     * <p>
     * The delay for attempt {@code n} is computed as: {@code random(0, min(baseDelay * 2^(n-1), maxDelay))}
     * <p>
     * Full jitter helps distribute retry attempts across clients, reducing the likelihood of retry storms.
     *
     * @param baseDelay the base delay for the first retry
     * @param maxDelay the maximum delay cap
     * @return an exponential backoff strategy with full jitter
     * @throws IllegalArgumentException if {@code baseDelay} is not positive, if {@code maxDelay} is not positive, or if
     *             {@code maxDelay} is less than {@code baseDelay}
     */
    static BackoffStrategy exponentialDelay(Duration baseDelay, Duration maxDelay) {
        Objects.requireNonNull(baseDelay, "baseDelay must not be null");
        Objects.requireNonNull(maxDelay, "maxDelay must not be null");
        if (baseDelay.isNegative() || baseDelay.isZero()) {
            throw new IllegalArgumentException("baseDelay must be positive, got: " + baseDelay);
        }
        if (maxDelay.isNegative() || maxDelay.isZero()) {
            throw new IllegalArgumentException("maxDelay must be positive, got: " + maxDelay);
        }
        if (maxDelay.compareTo(baseDelay) < 0) {
            throw new IllegalArgumentException("maxDelay must be greater than or equal to baseDelay, got: maxDelay="
                    + maxDelay + ", baseDelay=" + baseDelay);
        }
        return context -> {
            int attempt = context.attemptNumber();
            // cap shift at 30 to prevent overflow
            int shift = Math.min(attempt - 1, 30);
            long exponentialMillis = baseDelay.toMillis() * (1L << shift);
            long cappedMillis = Math.min(Math.max(exponentialMillis, baseDelay.toMillis()), maxDelay.toMillis());
            long jitteredMillis = ThreadLocalRandom.current().nextLong(cappedMillis + 1);
            return Duration.ofMillis(jitteredMillis);
        };
    }
}
