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

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * Holds the properties of an {@link IngestEvent}.
 * <p>
 * Convenience {@code put} overloads are provided for common Java types ({@link String}, {@code int}, {@code long},
 * {@code double}, {@code boolean}, {@link Instant}, and nested {@link Map}) and for building nested properties, each
 * wrapping the given value into an {@link IngestEventPropertyValue}. For other kinds of properties (e.g.
 * {@link IngestEventPropertyFile}), use {@link Builder#put(String, IngestEventProperty)} directly.
 *
 * @since 1.0.0
 */
public final class IngestEventProperties {

    private final Map<String, IngestEventProperty> properties;

    IngestEventProperties(Map<String, IngestEventProperty> properties) {
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    /**
     * Returns this instance's properties as a plain {@link Map}, unwrapped to their raw Java values for backward
     * compatibility with the pre-1.1.0 behavior (e.g. an {@link IngestEventPropertyValue} holding {@code "text"} is
     * returned as the {@link String} {@code "text"}, not as the wrapper itself). A property built from a deprecated
     * {@link PropertyArray} (via {@link Builder#put(String, PropertyArray)}) is returned as a {@link PropertyArray}
     * again where possible, for the same reason. Properties that aren't a simple {@link IngestEventPropertyValue} (e.g.
     * {@link IngestEventPropertyFile}) are returned as their raw {@link IngestEventProperty} instance, since they have
     * no pre-1.1.0 equivalent.
     *
     * @deprecated since 1.1.0, for removal: use {@link #getProperty(String)} to access a single typed
     *             {@link IngestEventProperty}, or {@link #forEach(BiConsumer)} to iterate over all of them.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public Map<String, Object> toMap() {
        var map = new LinkedHashMap<String, Object>();
        properties.forEach((key, property) -> map.put(key, toLegacyValue(property)));
        return Collections.unmodifiableMap(map);
    }

    private static Object toLegacyValue(IngestEventProperty property) {
        if (!(property instanceof IngestEventPropertyValue propertyValue)) {
            return property;
        }
        var value = propertyValue.value();
        if (propertyValue.type() == IngestEventPropertyValue.Type.UNKNOWN && value instanceof CICArray array) {
            var legacyArray = PropertyArray.tryFromCICArray(array);
            if (legacyArray != null) {
                return legacyArray;
            }
        }
        return value.toJavaValue();
    }

    /**
     * Returns the raw {@link IngestEventProperty} for the given key, or {@code null} if there's no such property.
     *
     * @since 1.1.0
     */
    @SuppressWarnings("unchecked")
    public <P extends IngestEventProperty> P getProperty(String key) {
        return (P) properties.get(key);
    }

    /**
     * Returns the given property's Java value, or {@code null} if there's no such property.
     *
     * @throws IllegalStateException if the property exists but isn't an {@link IngestEventPropertyValue}
     */
    protected Object getPropertyValue(String key) {
        var property = properties.get(key);
        if (property == null) {
            return null;
        }
        if (property instanceof IngestEventPropertyValue propertyValue) {
            return propertyValue.value().toJavaValue();
        }
        throw new IllegalStateException("Property " + key + " is not an "
                + IngestEventPropertyValue.class.getSimpleName() + ": " + property.getClass().getSimpleName());
    }

    /**
     * @since 1.1.0
     */
    public void forEach(BiConsumer<? super String, ? super IngestEventProperty> action) {
        properties.forEach(action);
    }

    public static Builder builder() {
        return new Builder(new LinkedHashMap<>());
    }

    static Builder builder(IngestEventProperties source) {
        return new Builder(new LinkedHashMap<>(source.properties));
    }

    public static final class Builder {

        private final Map<String, IngestEventProperty> properties;

        private Builder(Map<String, IngestEventProperty> properties) {
            this.properties = properties;
        }

        public Builder putNull(String key) {
            return put(key, IngestEventPropertyValue.builderNull().build());
        }

        public Builder put(String key, String value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, String[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        public Builder put(String key, int value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, int[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        public Builder put(String key, long value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, long[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        public Builder put(String key, double value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, double[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        public Builder put(String key, boolean value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, boolean[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, Instant value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, Instant[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, Map<String, ?> value) {
            return put(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, Map<String, ?>[] values) {
            return put(key, IngestEventPropertyValue.builder(values).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder put(String key, IngestEventProperty property) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(property, "property cannot be null");
            properties.put(key, property);
            return this;
        }

        /**
         * @implNote the resulting property always uses {@link IngestEventPropertyValue.Type#UNKNOWN}.
         * @deprecated since 1.1.0, in favor of the other {@code put} methods
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder put(String key, PropertyArray value) {
            Objects.requireNonNull(value, "value cannot be null");
            return put(key, toProperty(value));
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #put(String, Map)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder put(String key, IngestEventProperties value) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(value, "value cannot be null");
            return put(key, new IngestEventPropertyValue.Builder(IngestEventPropertyValue.Type.UNKNOWN,
                    toCICObject(value)).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #put(String, Map)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder put(String key, Consumer<IngestEventProperties.Builder> propertiesConsumer) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(propertiesConsumer, "propertiesConsumer cannot be null");
            var nestedBuilder = IngestEventProperties.builder();
            propertiesConsumer.accept(nestedBuilder);
            return put(key, new IngestEventPropertyValue.Builder(IngestEventPropertyValue.Type.UNKNOWN,
                    toCICObject(nestedBuilder.build())).build());
        }

        /**
         * Replaces each property with the result of applying the given function.
         * <p>
         * If the function returns {@code null} for a given key, that property is removed entirely.
         *
         * @since 1.1.0
         */
        public Builder replaceAll(
                BiFunction<? super String, ? super IngestEventProperty, ? extends IngestEventProperty> function) {
            Objects.requireNonNull(function, "function cannot be null");
            for (var it = properties.entrySet().iterator(); it.hasNext();) {
                var entry = it.next();
                var replacement = function.apply(entry.getKey(), entry.getValue());
                if (replacement == null) {
                    it.remove();
                } else {
                    entry.setValue(replacement);
                }
            }
            return this;
        }

        public IngestEventProperties build() {
            return new IngestEventProperties(properties);
        }

        private static CICObject toCICObject(IngestEventProperties nested) {
            var cicObject = CICObject.create();
            nested.forEach((key, property) -> putProperty(cicObject, key, property));
            return cicObject;
        }

        /**
         * @implNote this deprecated nesting idiom never had a notion of typed values (no {@code type}/{@code extras}
         *           were ever serialized for nested properties), so every nested {@link IngestEventPropertyValue} is
         *           always flattened to its raw value here, regardless of its {@link IngestEventPropertyValue.Type} or
         *           {@link IngestEventPropertyValue#extras()}.
         */
        private static void putProperty(CICObject target, String key, IngestEventProperty property) {
            if (property instanceof IngestEventPropertyValue propertyValue) {
                target.putNode(key, propertyValue.value());
            } else {
                throw new IllegalArgumentException("Unable to serialize property of type: " + property.getClass());
            }
        }

        private static IngestEventPropertyValue toProperty(PropertyArray value) {
            var elements = value.elements()
                                .stream()
                                .map(o -> o instanceof IngestEventProperties nested ? toCICObject(nested) : o)
                                .toList();
            return new IngestEventPropertyValue.Builder(IngestEventPropertyValue.Type.UNKNOWN,
                    CICArray.from(elements)).build();
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
