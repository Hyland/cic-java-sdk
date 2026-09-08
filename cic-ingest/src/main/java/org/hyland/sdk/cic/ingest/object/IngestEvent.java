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
package org.hyland.sdk.cic.ingest.object;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.util.StringUtils;

/**
 * @since 1.0.0
 */
public final class IngestEvent {

    protected final Type type;

    // nullable
    protected final String sourceId;

    protected final String objectId;

    protected final Instant date;

    protected final Map<String, IngestEventProperty> properties;

    protected IngestEvent(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.sourceId = builder.sourceId;
        this.objectId = StringUtils.requireNonBlank(builder.objectId, "objectId cannot be blank");
        this.date = Objects.requireNonNull(builder.date, "date cannot be null");
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(builder.properties));
    }

    public static Builder builder(Type type, String objectId) {
        return new Builder(type, objectId);
    }

    public Type type() {
        return type;
    }

    public Optional<String> sourceId() {
        return Optional.ofNullable(sourceId);
    }

    public String objectId() {
        return objectId;
    }

    public Instant date() {
        return date;
    }

    /**
     * Returns this event's properties using the typed {@link IngestEventProperty} model.
     *
     * @since 1.1.0
     */
    public Map<String, IngestEventProperty> typedProperties() {
        return properties;
    }

    /**
     * Returns this event's properties converted to the legacy {@link IngestEventProperties} model, on a best-effort
     * basis.
     *
     * @throws UnsupportedOperationException if a property cannot be represented in the legacy model (e.g. an
     *             {@link IngestEventPropertyFile})
     * @deprecated since 1.1.0, in favor of {@link #typedProperties()}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public IngestEventProperties properties() {
        var builder = IngestEventProperties.builder();
        properties.forEach((key, property) -> LegacyPropertyConverter.putLegacy(builder, key, property));
        return builder.build();
    }

    public Builder toBuilder() {
        return new Builder(type, objectId).sourceId(sourceId).date(date).typedProperties(properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IngestEvent) obj;
        return Objects.equals(this.type, that.type) && Objects.equals(this.sourceId, that.sourceId)
                && Objects.equals(this.objectId, that.objectId) && Objects.equals(this.date, that.date)
                && Objects.equals(this.properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sourceId, objectId, date, properties);
    }

    @Override
    public String toString() {
        return "IngestEvent[type=" + type + ", sourceId=" + sourceId + ", objectId=" + objectId + ", date=" + date
                + ", properties=REDACTED]";
    }

    public static final class Builder {

        private final Type type;

        private final String objectId;

        // nullable
        private String sourceId;

        private Instant date;

        private final Map<String, IngestEventProperty> properties;

        private Builder(Type type, String objectId) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.objectId = Objects.requireNonNull(objectId, "objectId cannot be null");
            this.date = Instant.now();
            this.properties = new LinkedHashMap<>();
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder date(Instant date) {
            this.date = Objects.requireNonNull(date, "date cannot be null");
            return this;
        }

        /**
         * Replaces this builder's properties with the typed {@link IngestEventProperty} model.
         *
         * @since 1.1.0
         */
        public Builder typedProperties(Map<String, IngestEventProperty> properties) {
            Objects.requireNonNull(properties, "properties cannot be null");
            this.properties.clear();
            this.properties.putAll(properties);
            return this;
        }

        /**
         * Replaces this builder's properties, converting the given legacy {@link IngestEventProperties} to the typed
         * {@link IngestEventProperty} model on a best-effort basis.
         *
         * @deprecated since 1.1.0, in favor of {@link #typedProperties(Map)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder properties(IngestEventProperties properties) {
            Objects.requireNonNull(properties, "properties cannot be null");
            this.properties.clear();
            properties.toMap().forEach((key, value) -> putLegacyProperty(key, value));
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperties(Map<String, IngestEventProperty> properties) {
            Objects.requireNonNull(properties, "properties cannot be null");
            properties.forEach(this::putProperty);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, IngestEventProperty property) {
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(property, "property cannot be null");
            this.properties.put(key, property);
            return this;
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, String, String...)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, String value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, String value, String... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, int, int...)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, int value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, int value, int... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, long, long...)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, long value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, long value, long... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, double, double...)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, double value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, double value, double... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, boolean, boolean...)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, boolean value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, boolean value, boolean... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Instant value) {
            return putProperty(key, IngestEventPropertyValue.builder(value).build());
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Instant value, Instant... values) {
            return putProperty(key, IngestEventPropertyValue.builder(value, values).build());
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, IngestEventProperty)} with
         *             {@link IngestEventPropertyValue}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, PropertyArray value) {
            Objects.requireNonNull(value, "value cannot be null");
            return putLegacyProperty(key, value);
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, IngestEventProperty)} with
         *             {@link IngestEventPropertyValue}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, IngestEventProperties value) {
            Objects.requireNonNull(value, "value cannot be null");
            return putLegacyProperty(key, value);
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, IngestEventProperty)} with
         *             {@link IngestEventPropertyValue}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, Consumer<IngestEventProperties.Builder> propertiesConsumer) {
            Objects.requireNonNull(propertiesConsumer, "propertiesConsumer cannot be null");
            var nestedBuilder = IngestEventProperties.builder();
            propertiesConsumer.accept(nestedBuilder);
            return putLegacyProperty(key, nestedBuilder.build());
        }

        private Builder putLegacyProperty(String key, Object legacyValue) {
            Objects.requireNonNull(key, "key cannot be null");
            return putProperty(key, LegacyPropertyConverter.toProperty(legacyValue));
        }

        /**
         * @since 1.1.0
         */
        public Builder replaceProperties(
                BiFunction<? super String, ? super IngestEventProperty, ? extends IngestEventProperty> function) {
            this.properties.replaceAll(function);
            return this;
        }

        public IngestEvent build() {
            return new IngestEvent(this);
        }
    }

    public enum Type {
        CREATE("create"), //
        CREATE_OR_UPDATE("createOrUpdate"), //
        UPDATE("update"), //
        DELETE("delete"); //

        protected final String label;

        Type(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static class Batch extends ArrayList<IngestEvent> {

        public static Batch of(IngestEvent... events) {
            var batch = new Batch();
            Collections.addAll(batch, events);
            return batch;
        }
    }

    /**
     * Best-effort conversion helpers between the legacy {@link IngestEventProperties} model and the typed
     * {@link IngestEventProperty} model.
     *
     * @since 1.1.0
     */
    private static final class LegacyPropertyConverter {

        private LegacyPropertyConverter() {
            // utility class
        }

        private static void putLegacy(IngestEventProperties.Builder builder, String key, IngestEventProperty property) {
            if (!(property instanceof IngestEventPropertyValue value)) {
                throw new UnsupportedOperationException(
                        "%s is not representable in the deprecated IngestEventProperties model".formatted(
                                property.getClass().getSimpleName()));
            }
            putConverted(builder, key, value.value().toJavaValue());
        }

        @SuppressWarnings("unchecked")
        private static void putConverted(IngestEventProperties.Builder builder, String key, Object javaValue) {
            if (javaValue == null) {
                builder.putNull(key);
            } else if (javaValue instanceof String s) {
                builder.put(key, s);
            } else if (javaValue instanceof Integer i) {
                builder.put(key, i.intValue());
            } else if (javaValue instanceof Long l) {
                builder.put(key, l.longValue());
            } else if (javaValue instanceof Double d) {
                builder.put(key, d.doubleValue());
            } else if (javaValue instanceof Boolean b) {
                builder.put(key, b.booleanValue());
            } else if (javaValue instanceof List<?> list) {
                builder.put(key, toPropertyArray(list));
            } else if (javaValue instanceof Map<?, ?> map) {
                builder.put(key, toNestedProperties((Map<String, Object>) map));
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported value type for legacy conversion: " + javaValue.getClass().getName());
            }
        }

        @SuppressWarnings("unchecked")
        private static PropertyArray toPropertyArray(List<?> list) {
            if (list.isEmpty()) {
                return PropertyArray.empty();
            }
            var first = list.get(0);
            if (first instanceof String) {
                return PropertyArray.of(list.toArray(new String[0]));
            } else if (first instanceof Integer) {
                return PropertyArray.of(list.stream().mapToInt(o -> (Integer) o).toArray());
            } else if (first instanceof Long) {
                return PropertyArray.of(list.stream().mapToLong(o -> (Long) o).toArray());
            } else if (first instanceof Double) {
                return PropertyArray.of(list.stream().mapToDouble(o -> (Double) o).toArray());
            } else if (first instanceof Boolean) {
                var array = new boolean[list.size()];
                for (var i = 0; i < array.length; i++) {
                    array[i] = (Boolean) list.get(i);
                }
                return PropertyArray.of(array);
            } else if (first instanceof Map) {
                var nested = list.stream()
                                 .map(o -> toNestedProperties((Map<String, Object>) o))
                                 .toArray(IngestEventProperties[]::new);
                return PropertyArray.of(nested);
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported array element type for legacy conversion: " + first.getClass().getName());
            }
        }

        private static IngestEventProperties toNestedProperties(Map<String, Object> map) {
            var nestedBuilder = IngestEventProperties.builder();
            map.forEach((key, value) -> putConverted(nestedBuilder, key, value));
            return nestedBuilder.build();
        }

        private static IngestEventProperty toProperty(Object legacyValue) {
            if (legacyValue instanceof PropertyArray array) {
                return fromPropertyArray(array);
            } else if (legacyValue instanceof IngestEventProperties nested) {
                return IngestEventPropertyValue.builder(toPlainMap(nested)).build();
            } else if (legacyValue instanceof String s) {
                return IngestEventPropertyValue.builder(s).build();
            } else if (legacyValue instanceof Integer i) {
                return IngestEventPropertyValue.builder(i.intValue()).build();
            } else if (legacyValue instanceof Long l) {
                return IngestEventPropertyValue.builder(l.longValue()).build();
            } else if (legacyValue instanceof Double d) {
                return IngestEventPropertyValue.builder(d.doubleValue()).build();
            } else if (legacyValue instanceof Boolean b) {
                return IngestEventPropertyValue.builder(b.booleanValue()).build();
            } else if (legacyValue == null) {
                return IngestEventPropertyValue.builderNull().build();
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported legacy property value type: " + legacyValue.getClass().getName());
            }
        }

        @SuppressWarnings("unchecked")
        private static IngestEventProperty fromPropertyArray(PropertyArray array) {
            var elements = array.elements();
            if (elements.isEmpty()) {
                return IngestEventPropertyValue.builder(new String[0]).build();
            }
            var first = elements.get(0);
            if (first instanceof String) {
                return IngestEventPropertyValue.builder(elements.toArray(new String[0])).build();
            } else if (first instanceof Integer) {
                return IngestEventPropertyValue.builder(elements.stream().mapToInt(o -> (Integer) o).toArray()).build();
            } else if (first instanceof Long) {
                return IngestEventPropertyValue.builder(elements.stream().mapToLong(o -> (Long) o).toArray()).build();
            } else if (first instanceof Double) {
                return IngestEventPropertyValue.builder(elements.stream().mapToDouble(o -> (Double) o).toArray())
                                               .build();
            } else if (first instanceof Boolean) {
                var booleanArray = new boolean[elements.size()];
                for (var i = 0; i < booleanArray.length; i++) {
                    booleanArray[i] = (Boolean) elements.get(i);
                }
                return IngestEventPropertyValue.builder(booleanArray).build();
            } else if (first instanceof IngestEventProperties) {
                var maps = elements.stream().map(o -> toPlainMap((IngestEventProperties) o)).toArray(Map[]::new);
                return IngestEventPropertyValue.builder(maps).build();
            } else {
                throw new UnsupportedOperationException(
                        "Unsupported PropertyArray element type: " + first.getClass().getName());
            }
        }

        private static Map<String, Object> toPlainMap(IngestEventProperties properties) {
            var result = new LinkedHashMap<String, Object>();
            properties.toMap().forEach((key, value) -> result.put(key, toPlainValue(value)));
            return result;
        }

        private static Object toPlainValue(Object value) {
            if (value instanceof PropertyArray array) {
                return array.elements().stream().map(LegacyPropertyConverter::toPlainValue).toList();
            } else if (value instanceof IngestEventProperties nested) {
                return toPlainMap(nested);
            } else {
                return value;
            }
        }
    }
}
