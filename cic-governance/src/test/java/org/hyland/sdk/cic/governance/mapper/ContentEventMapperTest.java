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
package org.hyland.sdk.cic.governance.mapper;

import java.time.Instant;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.governance.object.ContentEvent;
import org.hyland.sdk.cic.governance.object.ContentEventData;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.2.0
 */
public class ContentEventMapperTest {

    @Test
    public void testSerializeContentEvent() throws JSONException {
        var data = ContentEventData.builder("content-123", "env-1", "document.pdf", "primary")
                                   .putMetadata("title", "Test Document")
                                   .build();
        var event = ContentEvent.builder(ContentEvent.Type.CREATED, ContentEvent.DataSourceType.NUXEO, data)
                                .id("event-id-1")
                                .time(Instant.parse("2026-01-01T00:00:00Z"))
                                .build();

        var json = MapperService.writeAsString(event);
        var expected = """
                {
                  "specversion": "1.0",
                  "type": "cic.governance.content.v1.created",
                  "dataSourceType": "Nuxeo",
                  "id": "event-id-1",
                  "time": "2026-01-01T00:00:00Z",
                  "data": {
                    "contentId": "content-123",
                    "environmentId": "env-1",
                    "fileName": "document.pdf",
                    "contentFocus": "primary",
                    "metadata": {
                      "title": "Test Document"
                    }
                  }
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    public void testSerializeContentEventWithMinimalFields() throws JSONException {
        var data = ContentEventData.builder("content-123", "env-1", "document.pdf", "primary").build();
        var event = ContentEvent.builder(ContentEvent.Type.CREATED, ContentEvent.DataSourceType.NUXEO, data)
                                .id("event-id-1")
                                .time(Instant.parse("2026-01-01T00:00:00Z"))
                                .build();

        var json = MapperService.writeAsString(event);
        var expected = """
                {
                  "specversion": "1.0",
                  "type": "cic.governance.content.v1.created",
                  "dataSourceType": "Nuxeo",
                  "id": "event-id-1",
                  "time": "2026-01-01T00:00:00Z",
                  "data": {
                    "contentId": "content-123",
                    "environmentId": "env-1",
                    "fileName": "document.pdf",
                    "contentFocus": "primary"
                  }
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    public void testContentEventDataMetadataIsImmutable() {
        var data = ContentEventData.builder("content-123", "env-1", "document.pdf", "primary")
                                   .putMetadata("key", "value")
                                   .build();

        assertThatMetadataIsUnmodifiable(data.metadata());
    }

    private void assertThatMetadataIsUnmodifiable(Map<String, String> metadata) {
        try {
            metadata.put("new-key", "new-value");
            throw new AssertionError("Expected metadata to be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
