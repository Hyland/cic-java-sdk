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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingPage;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * @since 1.0.0
 */
class UserMappingMapperTest {

    @Test
    void testDeserializeUserMapping() {
        var json = """
                {
                  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "externalUserId": "ext-user-1",
                  "attributes": [
                    {"key": "email", "values": ["user@localhost"]}
                  ]
                }
                """;

        var result = MapperService.read(json, UserMapping.class);

        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result.userId());
        assertEquals("ext-user-1", result.externalUserId());
        assertEquals(1, result.attributes().size());
        assertEquals("email", result.attributes().get(0).key());
    }

    @Test
    void testDeserializeUserMappingPage() {
        var json = """
                {
                  "items": [
                    {
                      "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "externalUserId": "ext-user-1",
                      "attributes": []
                    }
                  ],
                  "next": null
                }
                """;

        var result = MapperService.read(json, UserMappingPage.class);

        assertEquals(1, result.data().size());
        assertEquals("ext-user-1", result.data().get(0).externalUserId());
        assertNull(result.pagination().nextCursor());
    }

    @Test
    void testSerializeUserMappingReplaceInput() throws Exception {
        var input = UserMappingReplaceInput.of("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"userId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890"}""", json, true);
    }
}
