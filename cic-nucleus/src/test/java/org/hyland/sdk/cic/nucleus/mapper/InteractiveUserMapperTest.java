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
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;

/**
 * @since 1.0.0
 */
class InteractiveUserMapperTest {

    @Test
    void testDeserializeInteractiveUser() {
        var json = """
                {
                  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "userName": "john.doe",
                  "email": "john.doe@example.com",
                  "externalId": "ext-123",
                  "preferredLanguage": "en-US"
                }
                """;

        var result = MapperService.read(json, InteractiveUser.class);

        assertEquals(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"), result.userId());
        assertEquals("john.doe", result.userName());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("ext-123", result.externalId());
        assertEquals("en-US", result.preferredLanguage());
    }

    @Test
    void testDeserializeInteractiveUserWithNullOptionalFields() {
        var json = """
                {
                  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "userName": null,
                  "email": null,
                  "externalId": null,
                  "preferredLanguage": null
                }
                """;

        var result = MapperService.read(json, InteractiveUser.class);

        assertEquals(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"), result.userId());
        assertNull(result.userName());
        assertNull(result.email());
        assertNull(result.externalId());
        assertNull(result.preferredLanguage());
    }

    @Test
    void testDeserializeInteractiveUserPaginatedList() {
        var json = """
                {
                  "users": [
                    {
                      "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "userName": "alice",
                      "email": "alice@localhost",
                      "externalId": "ext-alice",
                      "preferredLanguage": "fr-FR"
                    },
                    {
                      "userId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                      "userName": "bob",
                      "email": "bob@localhost",
                      "externalId": null,
                      "preferredLanguage": null
                    }
                  ],
                  "next": "cursor-token-abc"
                }
                """;

        var result = MapperService.read(json, InteractiveUser.PaginatedListOf.class);

        assertEquals(2, result.items().size());
        assertEquals("alice", result.items().get(0).userName());
        assertEquals("alice@localhost", result.items().get(0).email());
        assertEquals("ext-alice", result.items().get(0).externalId());
        assertEquals("fr-FR", result.items().get(0).preferredLanguage());
        assertEquals("bob", result.items().get(1).userName());
        assertNull(result.items().get(1).externalId());
        assertEquals("cursor-token-abc", result.next());
    }

    @Test
    void testDeserializeInteractiveUserPaginatedListWithNullNext() {
        var json = """
                {
                  "users": [],
                  "next": null
                }
                """;

        var result = MapperService.read(json, InteractiveUser.PaginatedListOf.class);

        assertNotNull(result.items());
        assertEquals(0, result.items().size());
        assertNull(result.next());
    }
}
