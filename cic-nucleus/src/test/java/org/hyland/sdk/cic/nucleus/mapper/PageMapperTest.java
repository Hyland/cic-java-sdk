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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.MembershipType;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMappingPage;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembershipPage;
import org.hyland.sdk.cic.nucleus.object.SystemIntegrationType;
import org.hyland.sdk.cic.nucleus.object.SystemOutputPage;
import org.hyland.sdk.cic.nucleus.object.UserMappingPage;

/**
 * Tests for all page mappers, verifying data extraction, cursor forwarding, and empty-page handling. Covers the shared
 * behaviour in {@link AbstractPageMapper}.
 *
 * @since 1.0.0
 */
class PageMapperTest {

    // --- SystemOutputPage ---

    @Test
    void testSystemOutputPageCursorExtracted() {
        var json = """
                {
                  "items": [
                    {"systemId": "sys-1", "name": "S1", "environmentId": "env-1", "systemType": "OnBase"}
                  ],
                  "next": "/systems?cursor=abc123&other=ignored"
                }
                """;

        var result = MapperService.read(json, SystemOutputPage.class);

        assertEquals(1, result.data().size());
        assertEquals("sys-1", result.data().get(0).systemId());
        assertEquals(SystemIntegrationType.ON_BASE, result.data().get(0).systemType());
        assertEquals("abc123", result.pagination().nextCursor());
        assertTrue(result.pagination().hasMore());
    }

    @Test
    void testSystemOutputPageEmptyItemsNullNext() {
        var json = """
                {"items": [], "next": null}
                """;

        var result = MapperService.read(json, SystemOutputPage.class);

        assertNotNull(result.data());
        assertTrue(result.data().isEmpty());
        assertNull(result.pagination().nextCursor());
        assertFalse(result.pagination().hasMore());
    }

    @Test
    void testSystemOutputPageMissingItemsField() {
        var json = """
                {"next": null}
                """;

        var result = MapperService.read(json, SystemOutputPage.class);

        assertNotNull(result.data());
        assertTrue(result.data().isEmpty());
    }

    // --- UserMappingPage ---

    @Test
    void testUserMappingPageCursorExtracted() {
        var json = """
                {
                  "items": [
                    {"userId": "u-1", "externalUserId": "ext-1", "attributes": []}
                  ],
                  "next": "/user-mappings?cursor=token-xyz"
                }
                """;

        var result = MapperService.read(json, UserMappingPage.class);

        assertEquals(1, result.data().size());
        assertEquals("ext-1", result.data().get(0).externalUserId());
        assertEquals("token-xyz", result.pagination().nextCursor());
        assertTrue(result.pagination().hasMore());
    }

    @Test
    void testUserMappingPageMultipleItems() {
        var json = """
                {
                  "items": [
                    {"userId": "u-1", "externalUserId": "ext-1", "attributes": []},
                    {"userId": "u-2", "externalUserId": "ext-2", "attributes": []}
                  ],
                  "next": null
                }
                """;

        var result = MapperService.read(json, UserMappingPage.class);

        assertEquals(2, result.data().size());
        assertEquals("ext-1", result.data().get(0).externalUserId());
        assertEquals("ext-2", result.data().get(1).externalUserId());
        assertNull(result.pagination().nextCursor());
    }

    // --- PrincipalUserMappingPage ---

    @Test
    void testPrincipalUserMappingPageCursorExtracted() {
        var json = """
                {
                  "items": [
                    {"systemId": "sys-1", "externalUserId": "ext-1", "attributes": []}
                  ],
                  "next": "/user-mappings?cursor=cursor-abc"
                }
                """;

        var result = MapperService.read(json, PrincipalUserMappingPage.class);

        assertEquals(1, result.data().size());
        assertEquals("sys-1", result.data().get(0).systemId());
        assertEquals("cursor-abc", result.pagination().nextCursor());
        assertTrue(result.pagination().hasMore());
    }

    @Test
    void testPrincipalUserMappingPageEmpty() {
        var json = """
                {"items": [], "next": null}
                """;

        var result = MapperService.read(json, PrincipalUserMappingPage.class);

        assertTrue(result.data().isEmpty());
        assertFalse(result.pagination().hasMore());
    }

    // --- PrincipalUserMembershipPage ---

    @Test
    void testPrincipalUserMembershipPageCursorExtracted() {
        var json = """
                {
                  "items": [
                    {
                      "externalGroupId": "g-1",
                      "systemId": "sys-1",
                      "membershipType": "Indirect",
                      "attributes": []
                    }
                  ],
                  "next": "/membership?cursor=mem-cursor"
                }
                """;

        var result = MapperService.read(json, PrincipalUserMembershipPage.class);

        assertEquals(1, result.data().size());
        assertEquals(MembershipType.INDIRECT, result.data().get(0).membershipType());
        assertEquals("mem-cursor", result.pagination().nextCursor());
        assertTrue(result.pagination().hasMore());
    }

    @Test
    void testPrincipalUserMembershipPageEmpty() {
        var json = """
                {"items": [], "next": null}
                """;

        var result = MapperService.read(json, PrincipalUserMembershipPage.class);

        assertTrue(result.data().isEmpty());
        assertNull(result.pagination().nextCursor());
    }

    // --- Cursor extraction edge cases (via InteractiveUserPage which uses "users" field) ---

    @Test
    void testCursorExtractedFromQueryStringWithMultipleParams() {
        var json = """
                {
                  "users": [],
                  "next": "/users?limit=10&cursor=encoded%2Fvalue&other=x"
                }
                """;

        var result = MapperService.read(json, org.hyland.sdk.cic.nucleus.object.InteractiveUserPage.class);

        assertEquals("encoded/value", result.pagination().nextCursor());
    }

    @Test
    void testNoCursorParamInNextUrl() {
        var json = """
                {
                  "users": [],
                  "next": "/users?limit=10"
                }
                """;

        var result = MapperService.read(json, org.hyland.sdk.cic.nucleus.object.InteractiveUserPage.class);

        assertNull(result.pagination().nextCursor());
        assertFalse(result.pagination().hasMore());
    }
}
