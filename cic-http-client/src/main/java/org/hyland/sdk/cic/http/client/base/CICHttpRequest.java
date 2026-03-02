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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.util.MapUtils;

/**
 * @since 1.0.0
 */
public interface CICHttpRequest {

    String HEAD = "HEAD";

    String GET = "GET";

    String POST = "POST";

    String PUT = "PUT";

    String DELETE = "DELETE";

    String method();

    String url();

    Map<String, List<String>> queryParameters();

    Map<String, List<String>> headers();

    Entity entity();

    static Builder builder(String method, String url) {
        if (method == null) {
            throw new CICSdkException("HTTP method is required");
        }
        if (url == null) {
            throw new CICSdkException("Url is required");
        }
        return new Builder(method, url);
    }

    class Builder {

        protected final String method;

        protected final String url;

        protected final Map<String, List<String>> queryParameters = new LinkedHashMap<>();

        protected final Map<String, List<String>> headers = new LinkedHashMap<>();

        protected Entity entity;

        public Builder(String method, String url) {
            this.method = method;
            this.url = url;
            if (url.contains("?")) {
                throw new CICSdkException(
                        "URL should not contain query parameters, use the queryParameter method instead");
            }
        }

        public Builder queryParameter(String name, String value) {
            queryParameters.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder header(String name, String value) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
            return this;
        }

        public Builder entity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public CICHttpRequest build() {
            // dereference the fields
            String method = this.method;
            String url = this.url;
            Map<String, List<String>> queryParameters = MapUtils.copyOfMultivalued(this.queryParameters);
            Map<String, List<String>> headers = MapUtils.copyOfMultivalued(this.headers);
            Entity entity = this.entity;
            return new CICHttpRequest() {

                @Override
                public String method() {
                    return method;
                }

                @Override
                public String url() {
                    return url;
                }

                @Override
                public Map<String, List<String>> queryParameters() {
                    return queryParameters;
                }

                @Override
                public Map<String, List<String>> headers() {
                    return headers;
                }

                @Override
                public Entity entity() {
                    return entity;
                }
            };
        }
    }

    sealed interface Entity {

    }

    record CICEntity(Object object) implements Entity {

    }

    record FormDataEntity(Map<String, List<String>> formData) implements Entity {

        public FormDataEntity {
            formData = MapUtils.copyOfMultivalued(formData);
        }

        public static FormDataEntity ofMap(Map<String, String> map) {
            return new FormDataEntity(
                    map.entrySet()
                       .stream()
                       .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.of(entry.getValue()))));
        }
    }

    record InputStreamEntity(Supplier<? extends InputStream> inputStream) implements Entity {

        public InputStreamEntity(InputStream inputStream) {
            this(() -> inputStream);
        }
    }
}
