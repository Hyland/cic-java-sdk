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
package org.hyland.sdk.cic.nucleus.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;

/**
 * @since 1.0.0
 */
class GroupOutputMapperTest {

    @Test
    void testDeserializeGroupOutput() {
        var json = """
                {
                  "externalGroupId": "ext-group-1",
                  "attributes": [
                    {"key": "department", "values": ["engineering"]}
                  ]
                }
                """;

        var result = MapperService.read(json, GroupOutput.class);

        assertEquals("ext-group-1", result.externalGroupId());
        assertEquals(1, result.attributes().size());
        assertEquals("department", result.attributes().get(0).key());
        assertEquals(List.of("engineering"), result.attributes().get(0).values());
    }

    @Test
    void testDeserializeGroupOutputWithNullAttributes() {
        var json = """
                {
                  "externalGroupId": "ext-group-2",
                  "attributes": null
                }
                """;

        var result = MapperService.read(json, GroupOutput.class);

        assertEquals("ext-group-2", result.externalGroupId());
        assertEquals(List.of(), result.attributes());
    }

    @Test
    void testDeserializeGroupOutputPaginatedList() {
        var json = """
                {
                  "items": [
                    {"externalGroupId": "group-1", "attributes": []},
                    {"externalGroupId": "group-2", "attributes": null}
                  ],
                  "next": "next-cursor"
                }
                """;

        var result = MapperService.read(json, GroupOutput.PaginatedListOf.class);

        assertEquals(2, result.items().size());
        assertEquals("group-1", result.items().get(0).externalGroupId());
        assertEquals("group-2", result.items().get(1).externalGroupId());
        assertEquals("next-cursor", result.next());
    }
}
