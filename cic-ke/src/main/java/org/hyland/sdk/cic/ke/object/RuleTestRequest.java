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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.object;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Test payload for {@code POST /config/options/rules/test}.
 *
 * @since 1.0.0
 */
public final class RuleTestRequest {

    private final Map<String, String> properties;

    private RuleTestRequest(Builder builder) {
        this.properties = Map.copyOf(builder.properties);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, String> properties() {
        return properties;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(properties, ((RuleTestRequest) obj).properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }

    public static final class Builder {

        private final Map<String, String> properties = new LinkedHashMap<>();

        public Builder property(String key, String value) {
            properties.put(key, value);
            return this;
        }

        public RuleTestRequest build() {
            return new RuleTestRequest(this);
        }
    }
}
