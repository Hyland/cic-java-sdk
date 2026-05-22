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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.DELETE;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.PUT;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.FormDataEntity;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.InputStreamEntity;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.http.client.retry.RetryContext;
import org.hyland.sdk.cic.http.client.retry.RetryPolicy;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;

/**
 * @since 1.0.0
 */
public abstract class AbstractHttpClient implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(AbstractHttpClient.class.getName());

    protected final HttpClient client;

    protected final String baseUrl;

    protected final Map<String, String> headers;

    protected final RetryPolicy retryPolicy;

    protected AbstractHttpClient(AbstractHttpClientBuilder<?, ?> builder) {
        var clientBuilder = HttpClient.newBuilder();
        if (builder.connectTimeout != null) {
            clientBuilder.connectTimeout(builder.connectTimeout);
        }
        this.client = clientBuilder.build();
        this.baseUrl = builder.baseUrl;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        this.retryPolicy = builder.retryPolicy;
    }

    protected CICHttpRequest.Builder requestBuilder(String method) {
        return requestBuilder(method, "");
    }

    protected CICHttpRequest.Builder requestBuilder(String method, String url) {
        var requestBuilder = CICHttpRequest.builder(method, url);
        appendHeaders(requestBuilder);
        return requestBuilder;
    }

    protected void appendHeaders(CICHttpRequest.Builder requestBuilder) {
        headers.forEach(requestBuilder::header);
    }

    protected CICHttpResponse<String> sendThenReadAsString(CICHttpRequest request) {
        return send(request, BodyHandlers.ofString());
    }

    protected <T> T sendThenMapAs(CICHttpRequest request, Class<T> type) {
        var response = send(request, BodyHandlers.ofString());
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
        return MapperService.read(response.body(), type);
    }

    private <T> CICHttpResponse<T> send(CICHttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) {
        var jdkRequest = toJdkHttpRequest(request);
        var jdkResponse = sendRawWithRetry(() -> jdkRequest, request.method(), bodyHandler,
                statusCode -> !ErrorUtils.isUnexpectedStatusCode(statusCode));
        return fromJdkHttpResponse(jdkResponse);
    }

    protected <T> HttpResponse<T> sendRawWithRetry(Supplier<HttpRequest> requestSupplier, String httpMethod,
            HttpResponse.BodyHandler<T> bodyHandler, IntPredicate successCondition) {
        int maxAttempts = retryPolicy.maxAttempts();
        CICSdkException lastException = null;
        HttpResponse<T> lastResponse = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                var jdkRequest = requestSupplier.get();
                var jdkResponse = client.send(jdkRequest, bodyHandler);
                int statusCode = jdkResponse.statusCode();

                if (successCondition.test(statusCode)) {
                    return jdkResponse;
                }

                lastResponse = jdkResponse;
                lastException = new CICSdkException("HTTP request failed with status code: " + statusCode);

                if (attempt < maxAttempts) {
                    var context = new RetryContext(attempt, httpMethod, statusCode, null);
                    if (retryPolicy.retryCondition().shouldRetry(context)) {
                        sleepBeforeRetry(context, attempt);
                        continue;
                    }
                }
                return jdkResponse;
            } catch (IOException e) {
                lastException = new CICSdkException("An error occurred during request execution", e);
                if (attempt < maxAttempts) {
                    var context = new RetryContext(attempt, httpMethod, 0, e);
                    if (retryPolicy.retryCondition().shouldRetry(context)) {
                        sleepBeforeRetry(context, attempt);
                        continue;
                    }
                }
                throw lastException;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CICSdkException("Interrupted while sending the request", e);
            }
        }
        if (lastResponse != null) {
            return lastResponse;
        }
        throw lastException;
    }

    private void sleepBeforeRetry(RetryContext context, int attempt) {
        var delay = retryPolicy.backoffStrategy().computeDelay(context);
        long delayMillis = Math.max(0, delay.toMillis());
        LOG.log(Level.FINE, "Retrying request, attempt {0}/{1} after {2}ms delay",
                new Object[] { attempt + 1, retryPolicy.maxAttempts(), delayMillis });
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new CICSdkException("Interrupted while waiting to retry the request", ie);
        }
    }

    protected HttpRequest toJdkHttpRequest(CICHttpRequest request) {
        // compute full url with query parameters
        String fullUrl = baseUrl + request.url();
        if (!request.queryParameters().isEmpty()) {
            fullUrl += "?" + request.queryParameters().entrySet().stream().<String> mapMulti((entry, downstream) -> {
                for (var value : entry.getValue()) {
                    downstream.accept(encodeKeyValueParameter(entry.getKey(), value));
                }
            }).collect(Collectors.joining("&"));
        }
        var jdkRequest = HttpRequest.newBuilder(URI.create(fullUrl)).timeout(Duration.ofSeconds(30));
        // append headers
        request.headers().forEach((name, values) -> values.forEach(value -> jdkRequest.header(name, value)));
        // set method and body
        switch (request.method()) {
            case GET -> jdkRequest.GET();
            case POST -> jdkRequest.POST(toJdkBody(request.entity()));
            case PUT -> jdkRequest.PUT(toJdkBody(request.entity()));
            case DELETE -> jdkRequest.DELETE();
            default -> jdkRequest.method(request.method(), toJdkBody(request.entity()));
        }
        return jdkRequest.build();
    }

    protected HttpRequest.BodyPublisher toJdkBody(CICHttpRequest.Entity entity) {
        if (entity instanceof CICEntity cicEntity) {
            return HttpRequest.BodyPublishers.ofString(MapperService.writeAsString(cicEntity.object()));
        } else if (entity instanceof FormDataEntity formDataEntity) {
            return HttpRequest.BodyPublishers.ofString(getFormDataAsString(formDataEntity.formData()));
        } else if (entity instanceof InputStreamEntity inputStreamEntity) {
            return HttpRequest.BodyPublishers.ofInputStream(inputStreamEntity.inputStream());
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    protected <T> CICHttpResponse<T> fromJdkHttpResponse(HttpResponse<T> response) {
        return new CICHttpResponse<>() {

            @Override
            public T body() {
                return response.body();
            }

            @Override
            public int statusCode() {
                return response.statusCode();
            }
        };
    }

    @Override
    public void close() {
    }

    protected static String getFormDataAsString(Map<String, List<String>> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, List<String>> singleEntry : formData.entrySet()) {
            String name = singleEntry.getKey();
            for (String value : singleEntry.getValue()) {
                if (!formBodyBuilder.isEmpty()) {
                    formBodyBuilder.append("&");
                }
                formBodyBuilder.append(encodeKeyValueParameter(name, value));
            }
        }
        return formBodyBuilder.toString();
    }

    protected static String encodeKeyValueParameter(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
