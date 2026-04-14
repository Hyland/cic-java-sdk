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

/**
 * Configures retry behavior for HTTP requests made by CIC SDK clients.
 * <p>
 * A {@code RetryPolicy} combines three components:
 * <ul>
 * <li>{@link #maxAttempts()} - the maximum number of attempts (initial request + retries)</li>
 * <li>{@link #retryCondition()} - determines whether a failed request should be retried</li>
 * <li>{@link #backoffStrategy()} - computes the delay between retry attempts</li>
 * </ul>
 * <h2>Usage Examples</h2>
 * <h3>Using the default policy</h3> The default policy is applied automatically when no policy is explicitly set. It
 * retries up to 3 attempts with exponential backoff (100ms base, 20s max) on server errors (5xx), rate limiting (429),
 * and I/O exceptions.
 *
 * <pre>{@code
 * IngestHttpClient client = IngestHttpClient.builder("https://api.example.com")
 *                                           .clientId("my-client-id")
 *                                           .clientSecret("my-secret")
 *                                           .build(); // default retry policy is applied automatically
 * }</pre>
 *
 * <h3>Custom retry policy</h3>
 *
 * <pre>{@code
 * RetryPolicy policy = RetryPolicy.builder()
 *                                 .maxAttempts(5)
 *                                 .backoffStrategy(BackoffStrategy.fixedDelay(Duration.ofSeconds(2)))
 *                                 .retryCondition(context -> context.statusCode() == 503)
 *                                 .build();
 *
 * IngestHttpClient client = IngestHttpClient.builder("https://api.example.com")
 *                                           .clientId("my-client-id")
 *                                           .clientSecret("my-secret")
 *                                           .retryPolicy(policy)
 *                                           .build();
 * }</pre>
 *
 * <h3>Disabling retries</h3>
 *
 * <pre>{@code
 * IngestHttpClient client = IngestHttpClient.builder("https://api.example.com")
 *                                           .clientId("my-client-id")
 *                                           .clientSecret("my-secret")
 *                                           .retryPolicy(RetryPolicy.none())
 *                                           .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class RetryPolicy {

    static final int DEFAULT_MAX_ATTEMPTS = 3;

    static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(100);

    static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(20);

    private final int maxAttempts;

    private final BackoffStrategy backoffStrategy;

    private final RetryCondition retryCondition;

    private RetryPolicy(Builder builder) {
        this.maxAttempts = builder.maxAttempts;
        this.backoffStrategy = builder.backoffStrategy;
        this.retryCondition = builder.retryCondition;
    }

    /**
     * Returns the maximum number of attempts (initial request + retries).
     * <p>
     * For example, a value of 3 means the request will be tried once and retried up to 2 times on failure.
     *
     * @return the maximum number of attempts
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the backoff strategy used to compute the delay between retry attempts.
     *
     * @return the backoff strategy
     */
    public BackoffStrategy backoffStrategy() {
        return backoffStrategy;
    }

    /**
     * Returns the condition that determines whether a failed request should be retried.
     *
     * @return the retry condition
     */
    public RetryCondition retryCondition() {
        return retryCondition;
    }

    /**
     * Returns the default retry policy.
     * <p>
     * The default policy uses:
     * <ul>
     * <li>Max attempts: 3</li>
     * <li>Backoff: exponential with full jitter (100ms base delay, 20s max delay)</li>
     * <li>Condition: retries on HTTP 429, 500, 502, 503, 504, and {@link java.io.IOException}</li>
     * </ul>
     *
     * @return the default retry policy
     */
    public static RetryPolicy defaultPolicy() {
        return builder().build();
    }

    /**
     * Returns a retry policy that disables retries entirely.
     * <p>
     * The returned policy has {@code maxAttempts} set to 1, meaning the request is executed once with no retries.
     *
     * @return a no-retry policy
     */
    public static RetryPolicy none() {
        return builder().maxAttempts(1).retryCondition(RetryCondition.none()).build();
    }

    /**
     * Creates a new builder with default settings.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link RetryPolicy}.
     */
    public static final class Builder {

        private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

        private BackoffStrategy backoffStrategy = BackoffStrategy.exponentialDelay(DEFAULT_BASE_DELAY,
                DEFAULT_MAX_DELAY);

        private RetryCondition retryCondition = RetryCondition.defaultCondition();

        private Builder() {
        }

        /**
         * Sets the maximum number of attempts (initial request + retries).
         * <p>
         * Must be at least 1. A value of 1 means no retries.
         *
         * @param maxAttempts the maximum number of attempts
         * @return this builder
         * @throws IllegalArgumentException if maxAttempts is less than 1
         */
        public Builder maxAttempts(int maxAttempts) {
            if (maxAttempts < 1) {
                throw new IllegalArgumentException("maxAttempts must be at least 1, got: " + maxAttempts);
            }
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Sets the backoff strategy for computing delay between retries.
         *
         * @param backoffStrategy the backoff strategy
         * @return this builder
         */
        public Builder backoffStrategy(BackoffStrategy backoffStrategy) {
            this.backoffStrategy = backoffStrategy;
            return this;
        }

        /**
         * Sets the condition that determines whether a failed request should be retried.
         *
         * @param retryCondition the retry condition
         * @return this builder
         */
        public Builder retryCondition(RetryCondition retryCondition) {
            this.retryCondition = retryCondition;
            return this;
        }

        /**
         * Builds the {@link RetryPolicy}.
         *
         * @return a new retry policy
         */
        public RetryPolicy build() {
            return new RetryPolicy(this);
        }
    }
}
