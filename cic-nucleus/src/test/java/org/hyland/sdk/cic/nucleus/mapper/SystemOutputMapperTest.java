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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.SystemIntegrationType;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;

/**
 * @since 1.0.0
 */
class SystemOutputMapperTest {

    @Test
    void testDeserializeSystemOutput() {
        var json = """
                {
                  "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "name": "TestSystem",
                  "environmentId": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
                  "systemType": "OnBase"
                }
                """;

        var result = MapperService.read(json, SystemOutput.class);

        assertEquals(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"), result.systemId());
        assertEquals("TestSystem", result.name());
        assertEquals(UUID.fromString("f1e2d3c4-b5a6-7890-fedc-ba0987654321"), result.environmentId());
        assertEquals(SystemIntegrationType.ON_BASE, result.systemType());
    }

    @Test
    void testDeserializeSystemOutputPaginatedList() {
        var json = """
                {
                  "items": [
                    {
                      "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "name": "System1",
                      "environmentId": "f1e2d3c4-b5a6-7890-fedc-ba0987654321",
                      "systemType": "Alfresco"
                    }
                  ],
                  "next": "cursor-token-123"
                }
                """;

        var result = MapperService.read(json, SystemOutput.PaginatedListOf.class);

        assertEquals(1, result.items().size());
        assertEquals("System1", result.items().get(0).name());
        assertEquals(SystemIntegrationType.ALFRESCO, result.items().get(0).systemType());
        assertEquals("cursor-token-123", result.next());
    }

    @Test
    void testDeserializeSystemOutputPaginatedListWithNullNext() {
        var json = """
                {
                  "items": [],
                  "next": null
                }
                """;

        var result = MapperService.read(json, SystemOutput.PaginatedListOf.class);

        assertNotNull(result.items());
        assertEquals(0, result.items().size());
        assertNull(result.next());
    }
}
