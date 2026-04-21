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
package org.hyland.sdk.cic.http.client.base;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy.Builder;

/**
 * @since 1.0.0
 */
public abstract class AbstractHttpClientBuilder<B extends AbstractHttpClientBuilder<B, C>, C extends AbstractHttpClient> {

    private static final String DEFAULT_USER_AGENT;

    static {
        DEFAULT_USER_AGENT = "CICJavaSDK/"
                + Optional.ofNullable(AbstractHttpClientBuilder.class.getPackage().getImplementationVersion())
                          .orElse("unknown");
    }

    protected final String baseUrl;

    protected final Map<String, String> headers = new LinkedHashMap<>();

    protected Duration connectTimeout;

    protected RetryPolicy retryPolicy = RetryPolicy.defaultPolicy();

    protected AbstractHttpClientBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
        header("User-Agent", DEFAULT_USER_AGENT);
    }

    /**
     * Sets the connection timeout for HTTP requests.
     *
     * @param connectTimeout the connection timeout duration; {@code null} means no timeout is set
     * @return this builder
     */
    public B connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return self();
    }

    /**
     * Sets the retry policy for HTTP requests.
     * <p>
     * By default, {@link RetryPolicy#defaultPolicy()} is used. Use {@link RetryPolicy#none()} to disable retries.
     *
     * @param retryPolicy the retry policy
     * @return this builder
     */
    public B retryPolicy(RetryPolicy retryPolicy) {
        Objects.requireNonNull(retryPolicy, "retryPolicy cannot be null");
        this.retryPolicy = retryPolicy;
        return self();
    }

    /**
     * Configures the retry policy for HTTP requests using a builder consumer.
     * Example:
     *
     * <pre>{@code
     * client.retryPolicy(
     *         policy -> policy.maxAttempts(5).backoffStrategy(BackoffStrategy.fixedDelay(Duration.ofSeconds(2))));
     * }</pre>
     *
     * @param consumer a consumer that configures the {@link RetryPolicy.Builder}; must not be null
     * @return this builder
     */
    public B retryPolicy(Consumer<Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        var builder = RetryPolicy.builder();
        consumer.accept(builder);
        this.retryPolicy = builder.build();
        return self();
    }

    /**
     * Sets an HTTP request header.
     * <p>
     *
     * @param name the header name
     * @param value the header value
     * @return this builder
     */
    public B header(String name, String value) {
        headers.put(name, value);
        return self();
    }

    /**
     * Overrides the {@code User-Agent} header sent with every request.
     * <p>
     * By default, the SDK sets {@code User-Agent} to {@code CICJavaSDK/<version>}.
     *
     * @param userAgent the user agent string
     * @return this builder
     */
    public B userAgent(String userAgent) {
        return header("User-Agent", userAgent);
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public abstract C build();
}
