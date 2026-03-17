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
import java.util.Optional;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public class IngestEvent {

    protected final Type type;

    protected final String objectId;

    protected Instant date;

    protected String sourceId;

    protected CICObject properties;

    public IngestEvent(Type type, String objectId) {
        this.type = type;
        this.date = Instant.now();
        this.objectId = objectId;
        this.sourceId = null;
        this.properties = CICObject.create();
    }

    public static Builder builder(Type type, String objectId) {
        return new Builder(type, objectId);
    }

    public Type type() {
        return type;
    }

    public Instant date() {
        return date;
    }

    public String objectId() {
        return objectId;
    }

    public Optional<String> sourceId() {
        return Optional.ofNullable(sourceId);
    }

    public CICObject properties() {
        return properties;
    }

    public static class Builder {

        private final IngestEvent event;

        private Builder(Type type, String objectId) {
            this.event = new IngestEvent(type, objectId);
        }

        public Builder sourceId(String sourceId) {
            event.sourceId = sourceId;
            return this;
        }

        public Builder date(Instant date) {
            event.date = date;
            return this;
        }

        public Builder properties(CICObject properties) {
            event.properties = properties != null ? properties : CICObject.create();
            return this;
        }

        public Builder putProperty(String key, String value) {
            event.properties.putString(key, value);
            return this;
        }

        public Builder putProperty(String key, int value) {
            event.properties.putInt(key, value);
            return this;
        }

        public Builder putProperty(String key, long value) {
            event.properties.putLong(key, value);
            return this;
        }

        public Builder putProperty(String key, boolean value) {
            event.properties.putBoolean(key, value);
            return this;
        }

        public Builder putProperty(String key, CICArray value) {
            event.properties.putArray(key, value);
            return this;
        }

        public Builder putProperty(String key, CICObject value) {
            event.properties.putObject(key, value);
            return this;
        }

        public IngestEvent build() {
            return event;
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
