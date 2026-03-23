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
import java.util.Objects;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public record IngestEvent(Type type, String sourceId, String objectId, Instant date, CICObject properties) {

    public IngestEvent {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(sourceId, "sourceId cannot be null");
        if (sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId cannot be blank");
        }
        Objects.requireNonNull(objectId, "objectId cannot be null");
        if (objectId.isBlank()) {
            throw new IllegalArgumentException("objectId cannot be blank");
        }
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(properties, "properties cannot be null");
    }

    public static Builder builder(Type type, String sourceId, String objectId) {
        return new Builder(type, sourceId, objectId);
    }

    public static final class Builder {

        private final Type type;

        private final String sourceId;

        private final String objectId;

        private Instant date;

        private CICObject properties;

        private Builder(Type type, String sourceId, String objectId) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.sourceId = Objects.requireNonNull(sourceId, "sourceId cannot be null");
            this.objectId = Objects.requireNonNull(objectId, "objectId cannot be null");
            this.date = Instant.now();
            this.properties = CICObject.create();
        }

        public Builder date(Instant date) {
            this.date = Objects.requireNonNull(date, "date cannot be null");
            return this;
        }

        /**
         * <b>Note:</b> This API directly exposes internal CIC object types and is considered beta.
         * Avoid building deep dependencies on it as the property format may evolve with future CIC REST API changes.
         */
        public Builder properties(CICObject properties) {
            this.properties = Objects.requireNonNullElseGet(properties, CICObject::create);
            return this;
        }

        public Builder putProperty(String key, String value) {
            Objects.requireNonNull(key, "property key cannot be null");
            Objects.requireNonNull(value, "property value cannot be null");
            this.properties.putString(key, value);
            return this;
        }

        public Builder putProperty(String key, int value) {
            Objects.requireNonNull(key, "property key cannot be null");
            this.properties.putInt(key, value);
            return this;
        }

        public Builder putProperty(String key, long value) {
            Objects.requireNonNull(key, "property key cannot be null");
            this.properties.putLong(key, value);
            return this;
        }

        public Builder putProperty(String key, double value) {
            Objects.requireNonNull(key, "property key cannot be null");
            this.properties.putDouble(key, value);
            return this;
        }

        public Builder putProperty(String key, boolean value) {
            Objects.requireNonNull(key, "property key cannot be null");
            this.properties.putBoolean(key, value);
            return this;
        }

        /**
         * <b>Note:</b> This API directly exposes internal CIC object types and is considered beta.
         * Avoid building deep dependencies on it as the property format may evolve with future CIC REST API changes.
         */
        public Builder putProperty(String key, CICArray value) {
            Objects.requireNonNull(key, "property key cannot be null");
            Objects.requireNonNull(value, "property value cannot be null");
            this.properties.putArray(key, value);
            return this;
        }

        /**
         * <b>Note:</b> This API directly exposes internal CIC object types and is considered beta.
         * Avoid building deep dependencies on it as the property format may evolve with future CIC REST API changes.
         */
        public Builder putProperty(String key, CICObject value) {
            Objects.requireNonNull(key, "property key cannot be null");
            Objects.requireNonNull(value, "property value cannot be null");
            this.properties.putObject(key, value);
            return this;
        }

        public IngestEvent build() {
            return new IngestEvent(type, sourceId, objectId, date, properties);
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

    public static class List extends ArrayList<IngestEvent> {

        public static List of(IngestEvent... events) {
            var list = new List();
            Collections.addAll(list, events);
            return list;
        }
    }
}
