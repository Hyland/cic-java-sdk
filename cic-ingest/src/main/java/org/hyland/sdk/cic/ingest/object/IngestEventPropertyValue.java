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

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;

/**
 * @since 1.1.0
 */
public final class IngestEventPropertyValue implements IngestEventProperty {

    /**
     * The CIC Ingest property type. We distinguish it from {@link CICNode} java type because Ingest type is the same
     * for singular element and array, and because datetime type is stored as {@link CICPrimitive.CICString}.
     */
    private final Type type;

    private final CICNode value;

    private final Map<String, CICNode> extras;

    private IngestEventPropertyValue(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.value = CICNode.unmodifiable(Objects.requireNonNull(builder.value, "value cannot be null"));
        var wrappedExtras = new LinkedHashMap<String, CICNode>();
        builder.extras.forEach((key, extraValue) -> wrappedExtras.put(key, CICNode.unmodifiable(extraValue)));
        this.extras = Collections.unmodifiableMap(wrappedExtras);
    }

    public static Builder builderNull() {
        return new Builder(Type.UNKNOWN, CICNode.from(null));
    }

    /**
     * Builds a property whose {@link Type} cannot be inferred from the given value (e.g. it doesn't map to one of the
     * other typed {@code builder(...)} overloads). The property is serialized with its {@code value} only, without a
     * {@code type}.
     * <p>
     * Prefer one of the other typed {@code builder(...)} overloads whenever the value's CIC type is known, so it is
     * explicitly declared on the resulting property.
     *
     * @since 1.1.0
     */
    public static Builder builder(Object value) {
        return new Builder(Type.UNKNOWN, CICNode.from(value));
    }

    public static Builder builder(boolean value) {
        return new Builder(Type.BOOLEAN, CICNode.from(value));
    }

    public static Builder builder(boolean[] values) {
        return new Builder(Type.BOOLEAN, CICArray.from(values));
    }

    public static Builder builder(Instant value) {
        Objects.requireNonNull(value, "value cannot be null");
        return new Builder(Type.DATETIME, CICNode.from(value.toString()));
    }

    public static Builder builder(Instant[] values) {
        Objects.requireNonNull(values, "values cannot be null");
        return new Builder(Type.DATETIME, CICArray.from(Stream.of(values).map(Instant::toString).toList()));
    }

    public static Builder builder(double value) {
        return new Builder(Type.FLOAT, CICNode.from(value));
    }

    public static Builder builder(double[] values) {
        return new Builder(Type.FLOAT, CICArray.from(values));
    }

    public static Builder builder(long value) {
        return new Builder(Type.INTEGER, CICNode.from(value));
    }

    public static Builder builder(long[] values) {
        return new Builder(Type.INTEGER, CICArray.from(values));
    }

    public static Builder builder(int value) {
        return new Builder(Type.INTEGER, CICNode.from(value));
    }

    public static Builder builder(int[] values) {
        return new Builder(Type.INTEGER, CICArray.from(values));
    }

    public static Builder builder(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        return new Builder(Type.STRING, CICNode.from(value));
    }

    public static Builder builder(String[] values) {
        Objects.requireNonNull(values, "values cannot be null");
        return new Builder(Type.STRING, CICArray.from(values));
    }

    public static Builder builder(Map<String, ?> value) {
        Objects.requireNonNull(value, "value cannot be null");
        return new Builder(Type.OBJECT, CICObject.from(value));
    }

    public static Builder builder(Map<String, ?>[] values) {
        Objects.requireNonNull(values, "values cannot be null");
        return new Builder(Type.OBJECT, CICArray.from(values));
    }

    public Type type() {
        return type;
    }

    /**
     * @implNote the returned {@link CICArray}/{@link CICObject} values are read-only (see
     *           {@link CICNode#unmodifiable(CICNode)}), so this property's state stays safe even if a caller casts and
     *           attempts to mutate them.
     */
    public CICNode value() {
        return value;
    }

    /**
     * @implNote see {@link #value()} regarding the read-only {@link CICArray}/{@link CICObject} values.
     */
    public Map<String, CICNode> extras() {
        return extras;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IngestEventPropertyValue other)) {
            return false;
        }
        return Objects.equals(type, other.type) && Objects.equals(value, other.value)
                && Objects.equals(extras, other.extras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, extras);
    }

    @Override
    public String toString() {
        if (extras.isEmpty()) {
            return "{type=%s,value=%s}".formatted(type, value);
        }
        return "{type=%s, value=%s,%s}".formatted(type, value, extras);
    }

    public static final class Builder {

        protected final Type type;

        protected final CICNode value;

        protected final Map<String, CICNode> extras = new LinkedHashMap<>();

        Builder(Type type, CICNode value) {
            this.type = type;
            this.value = value;
        }

        public Builder annotation(Serializable value) {
            return extra("annotation", value);
        }

        public Builder extra(String key, Serializable value) {
            Objects.requireNonNull(key, "key cannot be null");
            if ("type".equals(key) || "value".equals(key)) {
                throw new IllegalArgumentException("extra key cannot be a reserved field: " + key);
            }
            this.extras.put(key, CICNode.from(value));
            return this;
        }

        public Builder extras(Map<String, Serializable> extras) {
            Objects.requireNonNull(extras, "extras cannot be null");
            extras.forEach(this::extra);
            return this;
        }

        public IngestEventPropertyValue build() {
            return new IngestEventPropertyValue(this);
        }
    }

    public enum Type {
        BOOLEAN("boolean"),
        DATETIME("datetime"),
        FLOAT("float"),
        INTEGER("integer"),
        OBJECT("object"),
        STRING("string"),
        UNKNOWN("unknown");

        protected final String label;

        Type(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public static Type from(String label) {
            return Arrays.stream(Type.values())
                         .filter(type -> type.label.equals(label))
                         .findFirst()
                         .orElseThrow(
                                 () -> new IllegalArgumentException("Unknown ingest event property type: " + label));
        }
    }
}
