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
import java.util.function.Consumer;

/**
 * @since 1.0.0
 */
public record IngestEvent(Type type, String sourceId, String objectId, Instant date, IngestEventProperties properties) {

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

    public Builder toBuilder() {
        return new Builder(type, sourceId, objectId).date(date).properties(properties);
    }

    public static Builder builder(Type type, String sourceId, String objectId) {
        return new Builder(type, sourceId, objectId);
    }

    public static final class Builder {

        private final Type type;

        private final String sourceId;

        private final String objectId;

        private Instant date;

        private IngestEventProperties.Builder propertiesBuilder;

        private Builder(Type type, String sourceId, String objectId) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.sourceId = Objects.requireNonNull(sourceId, "sourceId cannot be null");
            this.objectId = Objects.requireNonNull(objectId, "objectId cannot be null");
            this.date = Instant.now();
            this.propertiesBuilder = IngestEventProperties.builder();
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

        public Builder putProperty(String key, String value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, int value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, long value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, double value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, boolean value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, PropertyArray value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, IngestEventProperties value) {
            propertiesBuilder.put(key, value);
            return this;
        }

        public Builder putProperty(String key, Consumer<IngestEventProperties.Builder> propertiesConsumer) {
            propertiesBuilder.put(key, propertiesConsumer);
            return this;
        }

        public IngestEvent build() {
            return new IngestEvent(type, sourceId, objectId, date, propertiesBuilder.build());
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
