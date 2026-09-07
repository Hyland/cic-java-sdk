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
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventProperty;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyFile;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyValue;

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
                    "title": {
                      "type": "string",
                      "value": "Test Document"
                    },
                    "version": {
                      "type": "integer",
                      "value": 1
                    },
                    "active": {
                      "type": "boolean",
                      "value": true
                    }
                  }
                }
                """;

        JSONAssert.assertEquals(json, expected, true);
    }

    @Test
    void testSerializeIngestEventComplex1() throws JSONException {
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
                      "type": "datetime",
                      "value": "2021-01-21T11:14:15.695Z",
                      "annotation": "dateCreated"
                    },
                    "nullValue": {
                      "type": "object",
                      "value": null
                    },
                    "name": {
                      "type": "string",
                      "value": "purchase-order-scan.pdf",
                      "annotation": "name"
                    },
                    "aspectsNames": {
                      "type": "string",
                      "value": ["versionable", "titled"],
                      "annotation": "aspects"
                    },
                    "content": {
                      "file": {
                        "id": "some-id",
                        "content-type": "application/pdf",
                        "content-metadata": {
                          "size": 531152,
                          "name": "purchase-order-scan.pdf",
                          "content-type": "application/pdf"
                        }
                      }
                    },
                    "PERMISSIONS": {
                      "type": "object",
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
    public void testSerializeIngestEventComplex2() throws JSONException {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE_OR_UPDATE, "1e47432c-872d-4404-b8a1-7aa85d03680a")
                               .sourceId("efffbf29-7d45-47ec-a7f0-7a9c5df8413b")
                               .date(Instant.ofEpochMilli(1778592730451L))
                               .putProperty("ingestProperty:type",
                                       IngestEventPropertyValue.builder("File").annotation("type").build())
                               .putProperty("ingestAncestor:Ids",
                                       IngestEventPropertyValue.builder(Map.of("allAncestorIds",
                                               List.of("df330bd4-aaa3-493c-8063-c10e6ce7f4a4",
                                                       "e11f2374-824f-49ce-b0a3-8081bccff79d",
                                                       "de14765d-d855-4b5f-8d25-46cd0b9d6856",
                                                       "1b305345-eb11-4afa-bd3a-e4d56914c071")))
                                                               .annotation("hierarchy")
                                                               .build())
                               .putProperty("dc:created",
                                       IngestEventPropertyValue.builder(Instant.parse("2026-04-23T07:10:33.391Z"))
                                                               .annotation("dateCreated")
                                                               .build())
                               .putProperty("dc:title",
                                       IngestEventPropertyValue.builder("test").annotation("name").build())
                               .putProperty("file:content",
                                       IngestEventPropertyFile.builder()
                                                              .id("DRY-RUN")
                                                              .contentType("image/png")
                                                              .size(1345829L)
                                                              .name("01 (1).png")
                                                              .digest("f756ee005ec8ed48368a2cda3f55e852")
                                                              .build())
                               .putProperty("files:files/0",
                                       IngestEventPropertyFile.builder()
                                                              .id("DRY-RUN")
                                                              .contentType("image/png")
                                                              .size(1345829L)
                                                              .name("01 (1).png")
                                                              .digest("f756ee005ec8ed48368a2cda3f55e852")
                                                              .build())
                               .putProperty("dc:creator",
                                       IngestEventPropertyValue.builder("Administrator")
                                                               .annotation("createdBy")
                                                               .build())
                               .putProperty("dc:modified",
                                       IngestEventPropertyValue.builder(Instant.parse("2026-04-23T07:13:19.610Z"))
                                                               .annotation("dateModified")
                                                               .build())
                               .putProperty("dc:lastContributor",
                                       IngestEventPropertyValue.builder("Administrator")
                                                               .annotation("modifiedBy")
                                                               .build())
                               .putProperty("dc:contributors",
                                       IngestEventPropertyValue.builder(new String[] { "Administrator" }).build())

                               .build();

        var json = MapperService.writeAsString(event);

        var expected = """
                   {
                     "objectId": "1e47432c-872d-4404-b8a1-7aa85d03680a",
                     "eventType": "createOrUpdate",
                     "sourceId": "efffbf29-7d45-47ec-a7f0-7a9c5df8413b",
                     "sourceTimestamp": 1778592730451,
                     "properties": {
                       "ingestProperty:type": {
                         "annotation": "type",
                         "type": "string",
                         "value": "File"
                       },
                       "ingestAncestor:Ids": {
                         "annotation": "hierarchy",
                         "type": "object",
                         "value": {
                           "allAncestorIds": [
                             "df330bd4-aaa3-493c-8063-c10e6ce7f4a4",
                             "e11f2374-824f-49ce-b0a3-8081bccff79d",
                             "de14765d-d855-4b5f-8d25-46cd0b9d6856",
                             "1b305345-eb11-4afa-bd3a-e4d56914c071"
                           ]
                         }
                       },
                       "dc:created": {
                         "annotation": "dateCreated",
                         "type": "datetime",
                         "value": "2026-04-23T07:10:33.391Z"
                       },
                       "dc:title": {
                         "annotation": "name",
                         "type": "string",
                         "value": "test"
                       },
                       "file:content": {
                         "file": {
                           "id": "DRY-RUN",
                           "content-type": "image/png",
                           "content-metadata": {
                             "size": 1345829,
                             "name": "01 (1).png",
                             "digest": "f756ee005ec8ed48368a2cda3f55e852",
                             "content-type": "image/png"
                           }
                         }
                       },
                       "files:files/0": {
                         "file": {
                           "id": "DRY-RUN",
                           "content-type": "image/png",
                           "content-metadata": {
                             "size": 1345829,
                             "name": "01 (1).png",
                             "digest": "f756ee005ec8ed48368a2cda3f55e852",
                             "content-type": "image/png"
                           }
                         }
                       },
                       "dc:creator": {
                         "annotation": "createdBy",
                         "type": "string",
                         "value": "Administrator"
                       },
                       "dc:modified": {
                         "annotation": "dateModified",
                         "type": "datetime",
                         "value": "2026-04-23T07:13:19.610Z"
                       },
                       "dc:lastContributor": {
                         "annotation": "modifiedBy",
                         "type": "string",
                         "value": "Administrator"
                       },
                       "dc:contributors": {
                         "type": "string",
                         "value": [
                           "Administrator"
                         ]
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

    private Map<String, IngestEventProperty> buildComplexProperties() {
        return Map.ofEntries(
                Map.entry("createdAt",
                        IngestEventPropertyValue.builder(Instant.parse("2021-01-21T11:14:15.695Z"))
                                                .annotation("dateCreated")
                                                .build()),
                Map.entry("nullValue", IngestEventPropertyValue.builderNull().build()),
                Map.entry("name",
                        IngestEventPropertyValue.builder("purchase-order-scan.pdf").annotation("name").build()),
                Map.entry("aspectsNames",
                        IngestEventPropertyValue.builder("versionable", "titled").annotation("aspects").build()),
                Map.entry("content",
                        IngestEventPropertyFile.builder()
                                               .id("some-id")
                                               .contentType("application/pdf")
                                               .size(531152L)
                                               .name("purchase-order-scan.pdf")
                                               .build()),
                Map.entry("PERMISSIONS", IngestEventPropertyValue
                                                                 .builder(Map.of("read",
                                                                         List.of(Map.of("id", "GROUP_EVERYONE", "type",
                                                                                 "GROUP")),
                                                                         "deny", List.of(), "principalsType",
                                                                         "effective"))
                                                                 .annotation("principals")
                                                                 .build()));
    }
}
