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
import java.util.Optional;

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public class IngestEvent {

    protected final Type type;

    protected final Instant date;

    protected final String objectId;

    protected String sourceId;

    protected CICObject properties;

    public IngestEvent(Type type, String objectId) {
        this.type = type;
        this.date = Instant.now();
        this.objectId = objectId;
        this.sourceId = null; // TODO check if needed, default to null in Nuxeo
        this.properties = CICObject.create();
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
}
