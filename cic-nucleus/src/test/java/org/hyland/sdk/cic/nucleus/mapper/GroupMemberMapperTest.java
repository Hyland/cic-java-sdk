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

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberPage;

/**
 * @since 1.0.0
 */
class GroupMemberMapperTest {

    @Test
    void testDeserializeGroupMemberWithUser() {
        var json = """
                {
                  "externalGroupId": "ext-group-1",
                  "memberExternalUserId": "ext-user-1",
                  "memberExternalGroupId": null
                }
                """;

        var result = MapperService.read(json, GroupMember.class);

        assertEquals("ext-group-1", result.externalGroupId());
        assertEquals("ext-user-1", result.memberExternalUserId());
        assertNull(result.memberExternalGroupId());
    }

    @Test
    void testDeserializeGroupMemberWithGroup() {
        var json = """
                {
                  "externalGroupId": "parent-group",
                  "memberExternalUserId": null,
                  "memberExternalGroupId": "child-group"
                }
                """;

        var result = MapperService.read(json, GroupMember.class);

        assertEquals("parent-group", result.externalGroupId());
        assertNull(result.memberExternalUserId());
        assertEquals("child-group", result.memberExternalGroupId());
    }

    @Test
    void testDeserializeGroupMemberPage() {
        var json = """
                {
                  "items": [
                    {
                      "externalGroupId": "ext-group-1",
                      "memberExternalUserId": "ext-user-1",
                      "memberExternalGroupId": null
                    }
                  ],
                  "next": "/group-members?cursor=next-page"
                }
                """;

        var result = MapperService.read(json, GroupMemberPage.class);

        assertEquals(1, result.data().size());
        assertEquals("ext-group-1", result.data().get(0).externalGroupId());
        assertEquals("next-page", result.pagination().nextCursor());
    }
}
