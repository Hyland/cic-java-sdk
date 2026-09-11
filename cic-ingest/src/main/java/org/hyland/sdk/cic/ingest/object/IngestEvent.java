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

    protected final IngestEventProperties properties;

    protected IngestEvent(Builder builder) {
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.sourceId = builder.sourceId;
        this.objectId = StringUtils.requireNonBlank(builder.objectId, "objectId cannot be blank");
        this.date = Objects.requireNonNull(builder.date, "date cannot be null");
        this.properties = builder.propertiesBuilder.build();
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

    public IngestEventProperties properties() {
        return properties;
    }

    public Builder toBuilder() {
        return new Builder(type, objectId).sourceId(sourceId).date(date).properties(properties);
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

        private IngestEventProperties.Builder propertiesBuilder;

        private Builder(Type type, String objectId) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.objectId = Objects.requireNonNull(objectId, "objectId cannot be null");
            this.date = Instant.now();
            this.propertiesBuilder = IngestEventProperties.builder();
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder date(Instant date) {
            this.date = Objects.requireNonNull(date, "date cannot be null");
            return this;
        }

        public Builder properties(IngestEventProperties properties) {
            this.propertiesBuilder = IngestEventProperties.builder(
                    Objects.requireNonNull(properties, "properties cannot be null"));
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putNullProperty(String key) {
            propertiesBuilder.putNull(key);
            return this;
        }

        public Builder putProperty(String key, String value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, String[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        public Builder putProperty(String key, int value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, int[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        public Builder putProperty(String key, long value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, long[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        public Builder putProperty(String key, double value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, double[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        public Builder putProperty(String key, boolean value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, boolean[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Instant value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Instant[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Map<String, ?> value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, Map<String, ?>[] values) {
            propertiesBuilder.put(key, values);
            return this;
        }

        /**
         * @since 1.1.0
         */
        public Builder putProperty(String key, IngestEventProperty property) {
            propertiesBuilder.put(key, property);
            return this;
        }

        /**
         * The resulting property always uses {@link IngestEventPropertyValue.Type#UNKNOWN}.
         *
         * @deprecated since 1.1.0, in favor of the other {@code putProperty} methods
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, PropertyArray value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, Map)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, IngestEventProperties value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        /**
         * @deprecated since 1.1.0, in favor of {@link #putProperty(String, Map)}
         */
        @Deprecated(since = "1.1.0", forRemoval = true)
        public Builder putProperty(String key, Consumer<IngestEventProperties.Builder> propertiesConsumer) {
            propertiesBuilder.put(key, propertiesConsumer);
            return this;
        }

        /**
         * Replaces each property with the result of applying the given function.
         * <p>
         * If the function returns {@code null} for a given key, that property is removed entirely.
         *
         * @since 1.1.0
         */
        public Builder replaceProperties(
                BiFunction<? super String, ? super IngestEventProperty, ? extends IngestEventProperty> function) {
            propertiesBuilder.replaceAll(function);
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
}
