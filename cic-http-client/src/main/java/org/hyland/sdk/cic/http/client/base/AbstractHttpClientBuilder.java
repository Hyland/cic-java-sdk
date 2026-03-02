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

/**
 * @since 1.0.0
 */
public abstract class AbstractHttpClientBuilder<B extends AbstractHttpClientBuilder<B, C>, C extends AbstractHttpClient> {

    protected final String baseUrl;

    protected final Map<String, String> headers = new LinkedHashMap<>();

    protected Duration connectTimeout;

    protected AbstractHttpClientBuilder(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public B connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return self();
    }

    public B header(String name, String value) {
        headers.put(name, value);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    public abstract C build();
}
