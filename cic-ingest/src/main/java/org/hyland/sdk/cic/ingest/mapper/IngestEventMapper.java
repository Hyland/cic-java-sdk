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
        cicObject.putString("sourceId", event.sourceId());
        cicObject.putObject("properties", event.properties());
        return cicObject;
    }

    static class ListMapper implements CICMapper<IngestEvent.List> {

        protected final IngestEventMapper innerMapper = new IngestEventMapper();

        @Override
        public CICNode toCICNode(IngestEvent.List events) {
            var cicArray = CICArray.create();
            for (IngestEvent event : events) {
                cicArray.addObject(innerMapper.toCICNode(event));
            }
            return cicArray;
        }
    }
}
