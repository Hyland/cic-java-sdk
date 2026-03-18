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
import java.util.Objects;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public record IngestEvent(Type type, String objectId, Instant date, String sourceId, CICObject properties) {

    public IngestEvent {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(objectId, "objectId cannot be null");
        if (objectId.isBlank()) {
            throw new IllegalArgumentException("objectId cannot be blank");
        }
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(properties, "properties cannot be null");

        var defensiveCopy = CICObject.create();
        properties.getProperties().forEach(defensiveCopy.getProperties()::put);
        properties = defensiveCopy;
    }

    public CICObject properties() {
        var copy = CICObject.create();
        properties.getProperties().forEach(copy.getProperties()::put);
        return copy;
    }

    public static Builder builder(Type type, String objectId) {
        return new Builder(type, objectId);
    }

    public static final class Builder {

        private final Type type;

        private final String objectId;

        private Instant date;

        private String sourceId;

        private CICObject properties;

        private Builder(Type type, String objectId) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.objectId = Objects.requireNonNull(objectId, "objectId cannot be null");
            this.date = Instant.now();
            this.sourceId = null;
            this.properties = CICObject.create();
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder date(Instant date) {
            this.date = Objects.requireNonNull(date, "date cannot be null");
            return this;
        }

        public Builder properties(CICObject properties) {
            if (properties == null) {
                this.properties = CICObject.create();
            } else {
                var copy = CICObject.create();
                properties.getProperties().forEach(copy.getProperties()::put);
                this.properties = copy;
            }
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

        public Builder putProperty(String key, CICArray value) {
            Objects.requireNonNull(key, "property key cannot be null");
            Objects.requireNonNull(value, "property value cannot be null");
            this.properties.putArray(key, value);
            return this;
        }

        public Builder putProperty(String key, CICObject value) {
            Objects.requireNonNull(key, "property key cannot be null");
            Objects.requireNonNull(value, "property value cannot be null");
            this.properties.putObject(key, value);
            return this;
        }

        public IngestEvent build() {
            return new IngestEvent(type, objectId, date, sourceId, properties);
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
            java.util.Collections.addAll(list, events);
            return list;
        }
    }
}
