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
 *     Damian Ujma <damian.ujma@hyland.com>
 */
package org.hyland.sdk.cic.ingest.object;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Holds the properties of an ingest event as plain Java types.
 * <p>
 * Supported value types: {@link String}, {@code int}, {@code long}, {@code double}, {@code boolean},
 * {@link PropertyArray} of any of those types (including nested {@link IngestEventProperties}), and nested
 * {@link IngestEventProperties}.
 *
 * @since 1.0.0
 */
public final class IngestEventProperties {

    private final Map<String, Object> properties;

    IngestEventProperties(Map<String, Object> properties) {
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public Map<String, Object> toMap() {
        return properties;
    }

    public static Builder builder() {
        return new Builder(new LinkedHashMap<>());
    }

    static Builder builder(IngestEventProperties source) {
        return new Builder(new LinkedHashMap<>(source.properties));
    }

    public static final class Builder {

        private final Map<String, Object> properties;

        private Builder(Map<String, Object> properties) {
            this.properties = properties;
        }

        public Builder put(String key, String value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, int value) {
            Objects.requireNonNull(key, "key cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, long value) {
            Objects.requireNonNull(key, "key cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, double value) {
            Objects.requireNonNull(key, "key cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, boolean value) {
            Objects.requireNonNull(key, "key cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, PropertyArray value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, IngestEventProperties value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            properties.put(key, value);
            return this;
        }

        public Builder put(String key, Consumer<IngestEventProperties.Builder> propertiesConsumer) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(propertiesConsumer, "propertiesConsumer cannot be null");
            var nestedBuilder = IngestEventProperties.builder();
            propertiesConsumer.accept(nestedBuilder);
            properties.put(key, nestedBuilder.build());
            return this;
        }

        public IngestEventProperties build() {
            return new IngestEventProperties(properties);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IngestEventProperties other)) {
            return false;
        }
        return properties.equals(other.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public String toString() {
        return properties.toString();
    }
}
