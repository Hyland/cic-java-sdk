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
package org.hyland.sdk.cic.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupMemberPage;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.GroupOutputPage;
import org.hyland.sdk.cic.nucleus.object.MembershipType;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMappingPage;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembershipPage;
import org.hyland.sdk.cic.nucleus.object.SystemIntegrationType;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.SystemOutputPage;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingPage;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * @since 1.0.0
 */
class SystemIntegrationServiceTest {

    private static final String SYSTEM_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    private static final String PRINCIPAL_USER_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901";

    private static final String USER_ID = "c3d4e5f6-a7b8-9012-cdef-123456789012";

    private TestNucleusHttpClient httpClient;

    private SystemIntegrationService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestNucleusHttpClient();
        service = new SystemIntegrationService(httpClient);
    }

    // --- Systems ---

    @Test
    void testListSystems() {
        httpClient.systemsPaginated = systemPage(
                List.of(new SystemOutput(SYSTEM_ID, "TestSystem", "env-id-1", SystemIntegrationType.ON_BASE)), null);

        var result = service.listSystems();

        assertEquals(1, result.data().size());
        assertEquals("TestSystem", result.data().get(0).name());
    }

    @Test
    void testListSystemsWithPagination() {
        httpClient.systemsPaginated = systemPage(List.of(), null);

        service.listSystems(r -> r.cursor("cursor-1").limit(5));

        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(5, httpClient.lastLimit);
    }

    @Test
    void testListSystemsPaginator() {
        httpClient.systemsPaginated = systemPage(
                List.of(new SystemOutput(SYSTEM_ID, "TestSystem", "env-id-1", SystemIntegrationType.ON_BASE)), null);

        var items = new ArrayList<SystemOutput>();
        service.listSystemsPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("TestSystem", items.get(0).name());
    }

    @Test
    void testListSystemsPaginatorExtractsCursorFromNextUrl() {
        var system1 = new SystemOutput("sys-id-1", "System1", "env-id-1", SystemIntegrationType.ON_BASE);
        var system2 = new SystemOutput("sys-id-2", "System2", "env-id-2", SystemIntegrationType.ON_BASE);
        var system3 = new SystemOutput("sys-id-3", "System3", "env-id-3", SystemIntegrationType.ON_BASE);

        httpClient.systemsPaginatedPages = List.of(systemPage(List.of(system1), "cursor1"),
                systemPage(List.of(system2), "cursor2"), systemPage(List.of(system3), null));

        var items = new ArrayList<SystemOutput>();
        service.listSystemsPaginator().forEach(items::add);

        assertEquals(3, items.size());
        assertEquals("System1", items.get(0).name());
        assertEquals("System2", items.get(1).name());
        assertEquals("System3", items.get(2).name());

        // exactly 3 calls: null → cursor1 → cursor2, then no 4th call because "next" was absent
        assertEquals(Arrays.asList(null, "cursor1", "cursor2"), httpClient.capturedCursors);
    }

    @Test
    void testGetSystem() {
        httpClient.systemOutput = new SystemOutput(SYSTEM_ID, "TestSystem", "env-id-1", SystemIntegrationType.ON_BASE);

        var result = service.getSystem(SYSTEM_ID);

        assertEquals(SYSTEM_ID, result.systemId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
    }

    @Test
    void testGetSystemNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.getSystem(null));
    }

    // --- SystemResource ---

    @Test
    void testSystemId() {
        assertEquals(SYSTEM_ID, service.system(SYSTEM_ID).id());
    }

    @Test
    void testSystemGet() {
        httpClient.systemOutput = new SystemOutput(SYSTEM_ID, "TestSystem", "env-id-1", SystemIntegrationType.LOCAL);

        var result = service.system(SYSTEM_ID).get();

        assertEquals(SYSTEM_ID, result.systemId());
    }

    @Test
    void testSystemListGroups() {
        httpClient.groupsPaginated = groupPage(List.of(new GroupOutput("ext-group-1", List.of())), null);

        var result = service.system(SYSTEM_ID).listGroups();

        assertEquals(1, result.data().size());
        assertEquals("ext-group-1", result.data().get(0).externalGroupId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
    }

    @Test
    void testSystemListGroupsWithPagination() {
        httpClient.groupsPaginated = groupPage(List.of(), null);

        service.system(SYSTEM_ID).listGroups(r -> r.cursor("cursor-1").limit(10));

        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(10, httpClient.lastLimit);
    }

    @Test
    void testSystemListGroupsPaginator() {
        httpClient.groupsPaginated = groupPage(List.of(new GroupOutput("ext-group-1", List.of())), null);

        var items = new ArrayList<GroupOutput>();
        service.system(SYSTEM_ID).listGroupsPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("ext-group-1", items.get(0).externalGroupId());
    }

    @Test
    void testSystemGetGroup() {
        httpClient.groupOutput = new GroupOutput("ext-group-1", List.of());

        var result = service.system(SYSTEM_ID).getGroup("ext-group-1");

        assertEquals("ext-group-1", result.externalGroupId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
    }

    @Test
    void testSystemCreateGroups() {
        var groups = List.of(GroupCreateInput.of("ext-group-1"));

        service.system(SYSTEM_ID).createGroups(groups);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals(groups, httpClient.lastGroupCreateInputs);
    }

    @Test
    void testSystemDeleteGroup() {
        service.system(SYSTEM_ID).deleteGroup("ext-group-1");

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
    }

    @Test
    void testSystemGetGroupAttributes() {
        httpClient.attributes = List.of(new Attribute("role", List.of("admin")));

        var result = service.system(SYSTEM_ID).getGroupAttributes("ext-group-1");

        assertEquals(1, result.size());
        assertEquals("role", result.get(0).key());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
    }

    @Test
    void testSystemGetGroupAttributesWithKeys() {
        httpClient.attributes = List.of(new Attribute("role", List.of("admin")));
        var keys = List.of("role");

        service.system(SYSTEM_ID).getGroupAttributes("ext-group-1", keys);

        assertEquals(keys, httpClient.lastKeys);
    }

    @Test
    void testSystemCreateGroupAttributes() {
        var attrs = List.of(AttributeInput.of("role", List.of("admin")));

        service.system(SYSTEM_ID).createGroupAttributes("ext-group-1", attrs);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
        assertEquals(attrs, httpClient.lastAttributeInputs);
    }

    @Test
    void testSystemReplaceGroupAttributes() {
        var attrs = List.of(AttributeInput.of("role", List.of("user")));

        service.system(SYSTEM_ID).replaceGroupAttributes("ext-group-1", attrs);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
        assertEquals(attrs, httpClient.lastAttributeInputs);
    }

    @Test
    void testSystemReplaceGroupAttributeValues() {
        service.system(SYSTEM_ID).replaceGroupAttributeValues("ext-group-1", "role", List.of("user"));

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
        assertEquals("role", httpClient.lastKey);
        assertEquals(List.of("user"), httpClient.lastValues);
    }

    @Test
    void testSystemDeleteGroupAttribute() {
        service.system(SYSTEM_ID).deleteGroupAttribute("ext-group-1", "role");

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
        assertEquals("role", httpClient.lastKey);
    }

    @Test
    void testSystemGetGroupMembers() {
        httpClient.groupMembersPaginated = groupMemberPage(List.of(new GroupMember("ext-group-1", "user-1", null)),
                null);

        var result = service.system(SYSTEM_ID).listGroupMembers();

        assertEquals(1, result.data().size());
        assertEquals("user-1", result.data().get(0).memberExternalUserId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
    }

    @Test
    void testSystemGetGroupMembersWithFilter() {
        httpClient.groupMembersPaginated = groupMemberPage(List.of(), null);

        service.system(SYSTEM_ID).listGroupMembers(r -> r.externalGroupId("ext-group-1").cursor("cursor-1").limit(20));

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastExternalGroupId);
        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(20, httpClient.lastLimit);
    }

    @Test
    void testSystemGetGroupMembersPaginator() {
        httpClient.groupMembersPaginated = groupMemberPage(List.of(new GroupMember("ext-group-1", "user-1", null)),
                null);

        var items = new ArrayList<GroupMember>();
        service.system(SYSTEM_ID).listGroupMembersPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("user-1", items.get(0).memberExternalUserId());
    }

    @Test
    void testSystemAssignGroupMembers() {
        var assignments = List.of(GroupMemberAssignmentInput.of("ext-group-1", "user-1"));

        service.system(SYSTEM_ID).assignGroupMembers(assignments);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals(assignments, httpClient.lastGroupMemberAssignmentInputs);
    }

    @Test
    void testSystemRemoveGroupMembers() {
        service.system(SYSTEM_ID).removeGroupMembers("ext-group-1", List.of("user-1"), null);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-group-1", httpClient.lastParentExternalGroupId);
        assertEquals(List.of("user-1"), httpClient.lastMemberExternalUserIds);
    }

    @Test
    void testSystemListUserMappings() {
        httpClient.userMappingsPaginated = userMappingPage(List.of(new UserMapping(USER_ID, "ext-user-1", List.of())),
                null);

        var result = service.system(SYSTEM_ID).listUserMappings();

        assertEquals(1, result.data().size());
        assertEquals("ext-user-1", result.data().get(0).externalUserId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
    }

    @Test
    void testSystemListUserMappingsWithPagination() {
        httpClient.userMappingsPaginated = userMappingPage(List.of(), null);

        service.system(SYSTEM_ID).listUserMappings(r -> r.cursor("cursor-1").limit(10));

        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(10, httpClient.lastLimit);
    }

    @Test
    void testSystemListUserMappingsPaginator() {
        httpClient.userMappingsPaginated = userMappingPage(List.of(new UserMapping(USER_ID, "ext-user-1", List.of())),
                null);

        var items = new ArrayList<UserMapping>();
        service.system(SYSTEM_ID).listUserMappingsPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("ext-user-1", items.get(0).externalUserId());
    }

    @Test
    void testSystemGetUserMapping() {
        httpClient.userMapping = new UserMapping(USER_ID, "ext-user-1", List.of());

        var result = service.system(SYSTEM_ID).getUserMapping("ext-user-1");

        assertEquals(USER_ID, result.userId());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
    }

    @Test
    void testSystemCreateUserMappings() {
        var mappings = List.of(UserMappingCreateInput.of(USER_ID, "ext-user-1"));

        service.system(SYSTEM_ID).createUserMappings(mappings);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals(mappings, httpClient.lastUserMappingCreateInputs);
    }

    @Test
    void testSystemUpdateUserMapping() {
        var input = UserMappingReplaceInput.of(USER_ID);

        service.system(SYSTEM_ID).updateUserMapping("ext-user-1", input);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
        assertEquals(input, httpClient.lastUserMappingReplaceInput);
    }

    @Test
    void testSystemDeleteUserMapping() {
        service.system(SYSTEM_ID).deleteUserMapping("ext-user-1");

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
    }

    @Test
    void testSystemGetUserMappingAttributes() {
        httpClient.attributes = List.of(new Attribute("email", List.of("user@example.com")));

        var result = service.system(SYSTEM_ID).getUserMappingAttributes("ext-user-1");

        assertEquals(1, result.size());
        assertEquals("email", result.get(0).key());
        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
    }

    @Test
    void testSystemGetUserMappingAttributesWithKeys() {
        httpClient.attributes = List.of();
        var keys = List.of("email");

        service.system(SYSTEM_ID).getUserMappingAttributes("ext-user-1", keys);

        assertEquals(keys, httpClient.lastKeys);
    }

    @Test
    void testSystemCreateUserMappingAttributes() {
        var attrs = List.of(AttributeInput.of("email", List.of("user@example.com")));

        service.system(SYSTEM_ID).createUserMappingAttributes("ext-user-1", attrs);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
        assertEquals(attrs, httpClient.lastAttributeInputs);
    }

    @Test
    void testSystemReplaceUserMappingAttributes() {
        var attrs = List.of(AttributeInput.of("email", List.of("updated@example.com")));

        service.system(SYSTEM_ID).replaceUserMappingAttributes("ext-user-1", attrs);

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
        assertEquals(attrs, httpClient.lastAttributeInputs);
    }

    @Test
    void testSystemReplaceUserMappingAttributeValues() {
        service.system(SYSTEM_ID).replaceUserMappingAttributeValues("ext-user-1", "email", List.of("new@example.com"));

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
        assertEquals("email", httpClient.lastKey);
        assertEquals(List.of("new@example.com"), httpClient.lastValues);
    }

    @Test
    void testSystemDeleteUserMappingAttribute() {
        service.system(SYSTEM_ID).deleteUserMappingAttribute("ext-user-1", "email");

        assertEquals(SYSTEM_ID, httpClient.lastSystemId);
        assertEquals("ext-user-1", httpClient.lastExternalUserId);
        assertEquals("email", httpClient.lastKey);
    }

    // --- PrincipalUserResource ---

    @Test
    void testPrincipalUserId() {
        assertEquals(PRINCIPAL_USER_ID, service.principalUser(PRINCIPAL_USER_ID).id());
    }

    @Test
    void testPrincipalUserGetUserMappings() {
        httpClient.principalUserMappingsPaginated = principalUserMappingPage(
                List.of(new PrincipalUserMapping(SYSTEM_ID, "ext-user-1", List.of())), null);

        var result = service.principalUser(PRINCIPAL_USER_ID).listUserMappings();

        assertEquals(1, result.data().size());
        assertEquals("ext-user-1", result.data().get(0).externalUserId());
        assertEquals(PRINCIPAL_USER_ID, httpClient.lastPrincipalUserId);
    }

    @Test
    void testPrincipalUserGetUserMappingsWithPagination() {
        httpClient.principalUserMappingsPaginated = principalUserMappingPage(List.of(), null);

        service.principalUser(PRINCIPAL_USER_ID).listUserMappings(r -> r.cursor("cursor-1").limit(10));

        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(10, httpClient.lastLimit);
    }

    @Test
    void testPrincipalUserGetUserMappingsPaginator() {
        httpClient.principalUserMappingsPaginated = principalUserMappingPage(
                List.of(new PrincipalUserMapping(SYSTEM_ID, "ext-user-1", List.of())), null);

        var items = new ArrayList<PrincipalUserMapping>();
        service.principalUser(PRINCIPAL_USER_ID).listUserMappingsPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("ext-user-1", items.get(0).externalUserId());
    }

    @Test
    void testPrincipalUserGetMembership() {
        httpClient.principalUserMembershipsPaginated = principalUserMembershipPage(
                List.of(new PrincipalUserMembership("ext-group-1", SYSTEM_ID, MembershipType.DIRECT, List.of())), null);

        var result = service.principalUser(PRINCIPAL_USER_ID).listMemberships();

        assertEquals(1, result.data().size());
        assertEquals(MembershipType.DIRECT, result.data().get(0).membershipType());
        assertEquals(PRINCIPAL_USER_ID, httpClient.lastPrincipalUserId);
    }

    @Test
    void testPrincipalUserGetMembershipWithPagination() {
        httpClient.principalUserMembershipsPaginated = principalUserMembershipPage(List.of(), null);

        service.principalUser(PRINCIPAL_USER_ID).listMemberships(r -> r.cursor("cursor-1").limit(10));

        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(10, httpClient.lastLimit);
    }

    @Test
    void testPrincipalUserGetMembershipPaginator() {
        httpClient.principalUserMembershipsPaginated = principalUserMembershipPage(
                List.of(new PrincipalUserMembership("ext-group-1", SYSTEM_ID, MembershipType.DIRECT, List.of())), null);

        var items = new ArrayList<PrincipalUserMembership>();
        service.principalUser(PRINCIPAL_USER_ID).listMembershipsPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals(MembershipType.DIRECT, items.get(0).membershipType());
    }

    // --- Null checks ---

    @Test
    void testSystemResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.system(null));
    }

    @Test
    void testPrincipalUserResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.principalUser(null));
    }

    private static SystemOutputPage systemPage(List<SystemOutput> items, String cursor) {
        return new SystemOutputPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static GroupOutputPage groupPage(List<GroupOutput> items, String cursor) {
        return new GroupOutputPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static GroupMemberPage groupMemberPage(List<GroupMember> items, String cursor) {
        return new GroupMemberPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static UserMappingPage userMappingPage(List<UserMapping> items, String cursor) {
        return new UserMappingPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static PrincipalUserMappingPage principalUserMappingPage(List<PrincipalUserMapping> items, String cursor) {
        return new PrincipalUserMappingPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static PrincipalUserMembershipPage principalUserMembershipPage(List<PrincipalUserMembership> items,
            String cursor) {
        return new PrincipalUserMembershipPage(items, new CursorPagination(cursor, cursor != null));
    }

    private static class TestNucleusHttpClient extends NucleusHttpClient {

        SystemOutputPage systemsPaginated;

        List<SystemOutputPage> systemsPaginatedPages;

        int pageIndex;

        List<String> capturedCursors = new ArrayList<>();

        SystemOutput systemOutput;

        GroupOutputPage groupsPaginated;

        GroupOutput groupOutput;

        GroupMemberPage groupMembersPaginated;

        UserMappingPage userMappingsPaginated;

        UserMapping userMapping;

        PrincipalUserMappingPage principalUserMappingsPaginated;

        PrincipalUserMembershipPage principalUserMembershipsPaginated;

        List<Attribute> attributes;

        String lastSystemId;

        String lastPrincipalUserId;

        String lastExternalGroupId;

        String lastParentExternalGroupId;

        String lastExternalUserId;

        String lastKey;

        String lastCursor;

        Integer lastLimit;

        List<String> lastKeys;

        List<String> lastValues;

        List<String> lastMemberExternalUserIds;

        List<GroupCreateInput> lastGroupCreateInputs;

        List<GroupMemberAssignmentInput> lastGroupMemberAssignmentInputs;

        List<UserMappingCreateInput> lastUserMappingCreateInputs;

        UserMappingReplaceInput lastUserMappingReplaceInput;

        List<AttributeInput> lastAttributeInputs;

        public TestNucleusHttpClient() {
            super(NucleusHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public SystemOutputPage listSystems(String cursor, Integer limit) {
            lastCursor = cursor;
            lastLimit = limit;
            capturedCursors.add(cursor);
            if (systemsPaginatedPages != null && pageIndex < systemsPaginatedPages.size()) {
                return systemsPaginatedPages.get(pageIndex++);
            }
            return systemsPaginated;
        }

        @Override
        public SystemOutput getSystem(String systemId) {
            lastSystemId = systemId;
            return systemOutput;
        }

        @Override
        public GroupOutputPage listGroups(String systemId, String cursor, Integer limit) {
            lastSystemId = systemId;
            lastCursor = cursor;
            lastLimit = limit;
            return groupsPaginated;
        }

        @Override
        public GroupOutput getGroup(String systemId, String externalGroupId) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            return groupOutput;
        }

        @Override
        public void createGroups(String systemId, List<GroupCreateInput> groups) {
            lastSystemId = systemId;
            lastGroupCreateInputs = groups;
        }

        @Override
        public void deleteGroup(String systemId, String externalGroupId) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
        }

        @Override
        public List<Attribute> getGroupAttributes(String systemId, String externalGroupId, List<String> keys) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastKeys = keys;
            return attributes;
        }

        @Override
        public void createGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attrs) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastAttributeInputs = attrs;
        }

        @Override
        public void replaceGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attrs) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastAttributeInputs = attrs;
        }

        @Override
        public void replaceGroupAttributeValues(String systemId, String externalGroupId, String key,
                List<String> values) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastKey = key;
            lastValues = values;
        }

        @Override
        public void deleteGroupAttribute(String systemId, String externalGroupId, String key) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastKey = key;
        }

        @Override
        public GroupMemberPage listGroupMembers(String systemId, String externalGroupId, String cursor, Integer limit) {
            lastSystemId = systemId;
            lastExternalGroupId = externalGroupId;
            lastCursor = cursor;
            lastLimit = limit;
            return groupMembersPaginated;
        }

        @Override
        public void assignGroupMembers(String systemId, List<GroupMemberAssignmentInput> assignments) {
            lastSystemId = systemId;
            lastGroupMemberAssignmentInputs = assignments;
        }

        @Override
        public void removeGroupMembers(String systemId, String parentExternalGroupId,
                List<String> memberExternalUserIds, List<String> memberExternalGroupIds) {
            lastSystemId = systemId;
            lastParentExternalGroupId = parentExternalGroupId;
            lastMemberExternalUserIds = memberExternalUserIds;
        }

        @Override
        public UserMappingPage listUserMappings(String systemId, String cursor, Integer limit) {
            lastSystemId = systemId;
            lastCursor = cursor;
            lastLimit = limit;
            return userMappingsPaginated;
        }

        @Override
        public UserMapping getUserMapping(String systemId, String externalUserId) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            return userMapping;
        }

        @Override
        public void createUserMappings(String systemId, List<UserMappingCreateInput> mappings) {
            lastSystemId = systemId;
            lastUserMappingCreateInputs = mappings;
        }

        @Override
        public void updateUserMapping(String systemId, String externalUserId, UserMappingReplaceInput input) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastUserMappingReplaceInput = input;
        }

        @Override
        public void deleteUserMapping(String systemId, String externalUserId) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
        }

        @Override
        public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId, List<String> keys) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastKeys = keys;
            return attributes;
        }

        @Override
        public void createUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attrs) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastAttributeInputs = attrs;
        }

        @Override
        public void replaceUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attrs) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastAttributeInputs = attrs;
        }

        @Override
        public void replaceUserMappingAttributeValues(String systemId, String externalUserId, String key,
                List<String> values) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastKey = key;
            lastValues = values;
        }

        @Override
        public void deleteUserMappingAttribute(String systemId, String externalUserId, String key) {
            lastSystemId = systemId;
            lastExternalUserId = externalUserId;
            lastKey = key;
        }

        @Override
        public PrincipalUserMappingPage listPrincipalUserMappings(String principalUserId, String cursor,
                Integer limit) {
            lastPrincipalUserId = principalUserId;
            lastCursor = cursor;
            lastLimit = limit;
            return principalUserMappingsPaginated;
        }

        @Override
        public PrincipalUserMembershipPage listPrincipalUserMemberships(String principalUserId, String cursor,
                Integer limit) {
            lastPrincipalUserId = principalUserId;
            lastCursor = cursor;
            lastLimit = limit;
            return principalUserMembershipsPaginated;
        }
    }
}
