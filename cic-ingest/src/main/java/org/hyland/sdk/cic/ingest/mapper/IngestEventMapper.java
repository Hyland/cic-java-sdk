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

import java.util.Map;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventProperty;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyFile;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyValue;

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

    private CICObject toPropertiesObject(Map<String, IngestEventProperty> properties) {
        var cicObject = CICObject.create();
        properties.forEach((key, property) -> {
            if (property instanceof IngestEventPropertyValue propertyValue) {
                var propertyObject = CICObject.create();
                propertyObject.putString("type", propertyValue.type().label());
                propertyObject.putNode("value", propertyValue.value());
                propertyValue.extras().forEach(propertyObject::putNode);
                cicObject.putObject(key, propertyObject);
            } else if (property instanceof IngestEventPropertyFile propertyBlob) {
                if (propertyBlob.getBlob().isPresent() && propertyBlob.id().isEmpty()) {
                    throw new IllegalStateException("the file has not been uploaded");
                }
                var propertyObject = CICObject.create();
                propertyBlob.id().ifPresent(id -> propertyObject.putString("id", id));
                propertyBlob.contentType().ifPresent(ct -> propertyObject.putString("content-type", ct));

                var metadataObject = CICObject.create();
                propertyBlob.size().ifPresent(size -> metadataObject.putLong("size", size));
                propertyBlob.name().ifPresent(name -> metadataObject.putString("name", name));
                propertyBlob.contentType().ifPresent(ct -> metadataObject.putString("content-type", ct));
                propertyBlob.digest().ifPresent(digest -> metadataObject.putString("digest", digest));
                propertyObject.putObject("content-metadata", metadataObject);

                var fileObject = CICObject.create();
                fileObject.putObject("file", propertyObject);
                cicObject.putObject(key, fileObject);
            } else {
                throw new IllegalArgumentException("Unable to serialize property of type: " + property.getClass());
            }
        });
        return cicObject;
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
