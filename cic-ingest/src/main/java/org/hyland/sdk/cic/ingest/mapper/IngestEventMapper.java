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
package org.hyland.sdk.cic.ingest.mapper;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventProperties;
import org.hyland.sdk.cic.ingest.object.PropertyArray;

/**
 * @since 1.0.0
 */
class IngestEventMapper implements CICMapper<IngestEvent> {

    @Override
    public CICObject toCICNode(IngestEvent event) {
        var cicObject = CICObject.create();
        cicObject.putString("eventType", event.type().label());
        cicObject.putLong("sourceTimestamp", event.date().toEpochMilli());
        cicObject.putString("objectId", event.objectId());
        cicObject.putString("sourceId",
                event.sourceId()
                     .orElseThrow(() -> new IllegalStateException("sourceId is null within event: " + event)));
        cicObject.putObject("properties", toPropertiesObject(event.properties()));
        return cicObject;
    }

    private CICObject toPropertiesObject(IngestEventProperties properties) {
        var cicObject = CICObject.create();
        properties.toMap().forEach((key, value) -> putValue(cicObject, key, value));
        return cicObject;
    }

    private void putValue(CICObject target, String key, Object value) {
        if (value instanceof String s) {
            target.putString(key, s);
        } else if (value instanceof Integer i) {
            target.putInt(key, i);
        } else if (value instanceof Long l) {
            target.putLong(key, l);
        } else if (value instanceof Double d) {
            target.putDouble(key, d);
        } else if (value instanceof Boolean b) {
            target.putBoolean(key, b);
        } else if (value instanceof PropertyArray array) {
            target.putArray(key, toArray(array));
        } else if (value instanceof IngestEventProperties nested) {
            target.putObject(key, toPropertiesObject(nested));
        } else {
            throw new IllegalArgumentException("Unsupported property value type: " + value.getClass().getName());
        }
    }

    private CICArray toArray(PropertyArray propertyArray) {
        var array = CICArray.create();
        for (var item : propertyArray.elements()) {
            if (item instanceof String s) {
                array.addString(s);
            } else if (item instanceof Integer i) {
                array.addInt(i);
            } else if (item instanceof Long l) {
                array.addLong(l);
            } else if (item instanceof Double d) {
                array.addDouble(d);
            } else if (item instanceof Boolean b) {
                array.addBoolean(b);
            } else if (item instanceof IngestEventProperties nested) {
                array.addObject(toPropertiesObject(nested));
            } else {
                throw new IllegalArgumentException("Unsupported array element type: " + item.getClass().getName());
            }
        }
        return array;
    }

    static class BatchMapper implements CICMapper<IngestEvent.Batch> {

        private final IngestEventMapper innerMapper = new IngestEventMapper();

        @Override
        public CICNode toCICNode(IngestEvent.Batch events) {
            var cicArray = CICArray.create();
            for (IngestEvent event : events) {
                cicArray.addObject(innerMapper.toCICNode(event));
            }
            return cicArray;
        }
    }
}
