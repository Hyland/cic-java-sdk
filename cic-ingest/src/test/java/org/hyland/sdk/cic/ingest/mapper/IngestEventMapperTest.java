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
 *     Damian Ujma <damian.ujma@hyland.com>
 */
package org.hyland.sdk.cic.ingest.mapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventProperties;
import org.hyland.sdk.cic.ingest.object.PropertyArray;

/**
 * @since 1.0.0
 */
class IngestEventMapperTest {

    @Test
    void testSerializeIngestEvent() throws JSONException {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .sourceId("source-1")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("title", "Test Document")
                               .putProperty("version", 1)
                               .putProperty("active", true)
                               .build();

        var json = MapperService.writeAsString(event);
        var expected = """
                {
                  "objectId": "doc123",
                  "sourceId": "source-1",
                  "eventType": "create",
                  "sourceTimestamp": 1609459200000,
                  "properties": {
                    "title": "Test Document",
                    "version": 1,
                    "active": true
                  }
                }
                """;

        JSONAssert.assertEquals(json, expected, true);
    }

    @Test
    void testSerializeComplexIngestEventMatchingExample() throws JSONException {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE_OR_UPDATE, "d71dd823-82c7-477c-8490-04cb0e826e65")
                               .sourceId("a1f3e7c0-d193-7023-ce1d-0a63de491876")
                               .date(Instant.ofEpochMilli(1611656982995L))
                               .properties(buildComplexProperties())
                               .build();

        var json = MapperService.writeAsString(event);
        var expected = """
                {
                  "objectId": "d71dd823-82c7-477c-8490-04cb0e826e65",
                  "sourceId": "a1f3e7c0-d193-7023-ce1d-0a63de491876",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1611656982995,
                  "properties": {
                    "createdAt": {
                      "value": "2021-01-21T11:14:15.695Z",
                      "annotation": "dateCreated"
                    },
                    "name": {
                      "value": "purchase-order-scan.pdf",
                      "annotation": "name"
                    },
                    "aspectsNames": {
                      "value": ["versionable", "titled"],
                      "annotation": "aspects"
                    },
                    "content": {
                      "file": {
                        "content-metadata": {
                          "size": 531152,
                          "name": "purchase-order-scan.pdf",
                          "content-type": "application/pdf"
                        }
                      }
                    },
                    "PERMISSIONS": {
                      "value": {
                        "read": [{
                          "id": "GROUP_EVERYONE",
                          "type": "GROUP"
                        }],
                        "deny": [],
                        "principalsType": "effective"
                      },
                      "annotation": "principals"
                    }
                  }
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    public void testMapperFactory() {
        var mapperService = new IngestMapperFactory();
        CICMapper<IngestEvent> mapper = mapperService.getMapper(IngestEvent.class);
        assertNotNull(mapper);
        assertInstanceOf(IngestEventMapper.class, mapper);
    }

    private IngestEventProperties buildComplexProperties() {
        return IngestEventProperties.builder()
                                    .put("createdAt",
                                            b -> b.put("value", "2021-01-21T11:14:15.695Z")
                                                  .put("annotation", "dateCreated"))
                                    .put("name",
                                            b -> b.put("value", "purchase-order-scan.pdf").put("annotation", "name"))
                                    .put("aspectsNames",
                                            b -> b.put("value", PropertyArray.of("versionable", "titled"))
                                                  .put("annotation", "aspects"))
                                    .put("content",
                                            b -> b.put("file",
                                                    c -> c.put("content-metadata",
                                                            d -> d.put("size", 531152L)
                                                                  .put("name", "purchase-order-scan.pdf")
                                                                  .put("content-type", "application/pdf"))))
                                    .put("PERMISSIONS",
                                            b -> b.put("value", c -> c.put("read",
                                                    PropertyArray.of(IngestEventProperties.builder()
                                                                                          .put("id", "GROUP_EVERYONE")
                                                                                          .put("type", "GROUP")
                                                                                          .build()))
                                                                      .put("deny", PropertyArray.empty())
                                                                      .put("principalsType", "effective"))
                                                  .put("annotation", "principals"))
                                    .build();
    }
}
