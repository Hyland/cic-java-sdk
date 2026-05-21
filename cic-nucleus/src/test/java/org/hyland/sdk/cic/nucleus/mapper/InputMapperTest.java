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

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;

/**
 * @since 1.0.0
 */
class InputMapperTest {

    @Test
    void testSerializeGroupCreateInput() throws Exception {
        var input = GroupCreateInput.of("ext-group-1", List.of(AttributeInput.of("role", List.of("admin"))));

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"externalGroupId":"ext-group-1","attributes":[{"key":"role","values":["admin"]}]}""", json, true);
    }

    @Test
    void testSerializeGroupCreateInputWithoutAttributes() throws Exception {
        var input = GroupCreateInput.of("ext-group-1");

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"externalGroupId":"ext-group-1"}""", json, true);
    }

    @Test
    void testSerializeGroupMemberAssignmentInput() throws Exception {
        var input = GroupMemberAssignmentInput.of("ext-group-1", "ext-user-1");

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"externalGroupId":"ext-group-1","memberExternalUserId":"ext-user-1"}""", json, true);
    }

    @Test
    void testSerializeUserMappingCreateInput() throws Exception {
        var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        var input = UserMappingCreateInput.of(userId, "ext-user-1");

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"userId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","externalUserId":"ext-user-1"}""", json, true);
    }

    @Test
    void testSerializeUserMappingCreateInputWithAttributes() throws Exception {
        var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        var input = UserMappingCreateInput.of(userId, "ext-user-1",
                List.of(AttributeInput.of("email", List.of("user@localhost"))));

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"userId":"a1b2c3d4-e5f6-7890-abcd-ef1234567890","externalUserId":"ext-user-1",\
                "attributes":[{"key":"email","values":["user@localhost"]}]}""", json, true);
    }
}
