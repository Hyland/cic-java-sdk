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
package org.hyland.sdk.cic.governance.object;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * An event sent to the CIC Governance {@code POST /api/records/event} endpoint.
 *
 * @since 1.2.0
 */
public final class ContentEvent {

    private static final String DEFAULT_SPEC_VERSION = "1.0";

    protected final String specversion;

    protected final Type type;

    protected final DataSourceType dataSourceType;

    protected final String id;

    protected final Instant time;

    protected final ContentEventData data;

    protected ContentEvent(Builder builder) {
        this.specversion = Objects.requireNonNull(builder.specversion, "specversion cannot be null");
        this.type = Objects.requireNonNull(builder.type, "type cannot be null");
        this.dataSourceType = Objects.requireNonNull(builder.dataSourceType, "dataSourceType cannot be null");
        this.id = Objects.requireNonNull(builder.id, "id cannot be null");
        this.time = Objects.requireNonNull(builder.time, "time cannot be null");
        this.data = Objects.requireNonNull(builder.data, "data cannot be null");
    }

    public static Builder builder(Type type, DataSourceType dataSourceType, ContentEventData data) {
        return new Builder(type, dataSourceType, data);
    }

    public String specversion() {
        return specversion;
    }

    public Type type() {
        return type;
    }

    public DataSourceType dataSourceType() {
        return dataSourceType;
    }

    public String id() {
        return id;
    }

    public Instant time() {
        return time;
    }

    public ContentEventData data() {
        return data;
    }

    public Builder toBuilder() {
        return new Builder(type, dataSourceType, data).specversion(specversion).id(id).time(time);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ContentEvent) obj;
        return Objects.equals(this.specversion, that.specversion) && this.type == that.type
                && this.dataSourceType == that.dataSourceType && Objects.equals(this.id, that.id)
                && Objects.equals(this.time, that.time) && Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specversion, type, dataSourceType, id, time, data);
    }

    @Override
    public String toString() {
        return "ContentEvent[specversion=" + specversion + ", type=" + type + ", dataSourceType=" + dataSourceType
                + ", id=" + id + ", time=" + time + ", data=" + data + "]";
    }

    public static final class Builder {

        private final Type type;

        private final DataSourceType dataSourceType;

        private final ContentEventData data;

        private String specversion;

        private String id;

        private Instant time;

        private Builder(Type type, DataSourceType dataSourceType, ContentEventData data) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.dataSourceType = Objects.requireNonNull(dataSourceType, "dataSourceType cannot be null");
            this.data = Objects.requireNonNull(data, "data cannot be null");
            this.specversion = DEFAULT_SPEC_VERSION;
            this.id = UUID.randomUUID().toString();
            this.time = Instant.now();
        }

        public Builder specversion(String specversion) {
            this.specversion = Objects.requireNonNull(specversion, "specversion cannot be null");
            return this;
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNull(id, "id cannot be null");
            return this;
        }

        public Builder time(Instant time) {
            this.time = Objects.requireNonNull(time, "time cannot be null");
            return this;
        }

        public ContentEvent build() {
            return new ContentEvent(this);
        }
    }

    /**
     * The {@code type} values accepted by the CIC Governance {@code POST /api/records/event} endpoint.
     */
    public enum Type {
        CREATED("cic.governance.content.v1.created"),
        MODIFIED("cic.governance.content.v1.modified"),
        DELETED("cic.governance.content.v1.deleted");

        protected final String label;

        Type(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * The {@code dataSourceType} values accepted by the CIC Governance {@code POST /api/records/event} endpoint.
     */
    public enum DataSourceType {
        HX_PR("HxPR"),
        ON_BASE("OnBase"),
        ALFRESCO("Alfresco"),
        NUXEO("Nuxeo");

        protected final String label;

        DataSourceType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }
}
