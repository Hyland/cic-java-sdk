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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.MembershipType;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;

/**
 * @since 1.0.0
 */
class PrincipalUserMapperTest {

    @Test
    void testDeserializePrincipalUserMapping() {
        var json = """
                {
                  "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "externalUserId": "ext-user-1",
                  "attributes": [
                    {"key": "role", "values": ["admin"]}
                  ]
                }
                """;

        var result = MapperService.read(json, PrincipalUserMapping.class);

        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result.systemId());
        assertEquals("ext-user-1", result.externalUserId());
        assertEquals(1, result.attributes().size());
    }

    @Test
    void testDeserializePrincipalUserMappingPaginatedList() {
        var json = """
                {
                  "items": [
                    {
                      "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "externalUserId": "ext-user-1",
                      "attributes": []
                    }
                  ],
                  "next": "cursor-abc"
                }
                """;

        var result = MapperService.read(json, PrincipalUserMapping.PaginatedListOf.class);

        assertEquals(1, result.items().size());
        assertEquals("cursor-abc", result.next());
    }

    @Test
    void testDeserializePrincipalUserMembership() {
        var json = """
                {
                  "externalGroupId": "ext-group-1",
                  "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "membershipType": "Direct",
                  "attributes": [
                    {"key": "level", "values": ["senior"]}
                  ]
                }
                """;

        var result = MapperService.read(json, PrincipalUserMembership.class);

        assertEquals("ext-group-1", result.externalGroupId());
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result.systemId());
        assertEquals(MembershipType.DIRECT, result.membershipType());
        assertEquals(1, result.attributes().size());
    }

    @Test
    void testDeserializePrincipalUserMembershipIndirect() {
        var json = """
                {
                  "externalGroupId": "ext-group-2",
                  "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "membershipType": "Indirect",
                  "attributes": []
                }
                """;

        var result = MapperService.read(json, PrincipalUserMembership.class);

        assertEquals(MembershipType.INDIRECT, result.membershipType());
    }

    @Test
    void testDeserializePrincipalUserMembershipPaginatedList() {
        var json = """
                {
                  "items": [
                    {
                      "externalGroupId": "ext-group-1",
                      "systemId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                      "membershipType": "Direct",
                      "attributes": []
                    }
                  ],
                  "next": null
                }
                """;

        var result = MapperService.read(json, PrincipalUserMembership.PaginatedListOf.class);

        assertEquals(1, result.items().size());
    }

    @Test
    void testMembershipTypeFromValueUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> MembershipType.fromValue("Unknown"));
    }

    @Test
    void testMembershipTypeFromValueNullThrows() {
        assertThrows(NullPointerException.class, () -> MembershipType.fromValue(null));
    }

    @Test
    void testSystemIntegrationTypeFromValueUnknownThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> org.hyland.sdk.cic.nucleus.object.SystemIntegrationType.fromValue("Unknown"));
    }
}
