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

import java.util.List;
import java.util.Objects;

import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.PaginatedList;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * High-level service for CIC System Integrations API operations.
 *
 * @since 1.0.0
 */
public class SystemIntegrationService {

    protected final NucleusHttpClient httpClient;

    public SystemIntegrationService(NucleusHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns a {@link SystemResource} handle bound to the given system ID.
     *
     * @param systemId the system ID
     * @return the system resource handle
     * @throws NullPointerException if systemId is null
     */
    public SystemResource system(String systemId) {
        return new SystemResource(httpClient, Objects.requireNonNull(systemId, "systemId cannot be null"));
    }

    /**
     * Returns a {@link PrincipalUserResource} handle bound to the given principal user ID.
     *
     * @param principalUserId the principal user ID
     * @return the principal user resource handle
     * @throws NullPointerException if principalUserId is null
     */
    public PrincipalUserResource principalUser(String principalUserId) {
        return new PrincipalUserResource(httpClient,
                Objects.requireNonNull(principalUserId, "principalUserId cannot be null"));
    }

    // --- Systems ---

    public SystemOutput.PaginatedListOf listSystems() {
        return httpClient.listSystems(null, null);
    }

    public SystemOutput.PaginatedListOf listSystems(String cursor, Integer limit) {
        return httpClient.listSystems(cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all systems across all pages.
     *
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<SystemOutput> listSystemsPaginator() {
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listSystems(cursor, null)));
    }

    public SystemOutput getSystem(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getSystem(systemId);
    }

    // --- Groups ---

    public GroupOutput.PaginatedListOf listGroups(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroups(systemId, null, null);
    }

    public GroupOutput.PaginatedListOf listGroups(String systemId, String cursor, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroups(systemId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all groups for the given system across all pages.
     *
     * @param systemId the system ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<GroupOutput> listGroupsPaginator(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, null)));
    }

    public GroupOutput getGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroup(systemId, externalGroupId);
    }

    public void createGroups(String systemId, List<GroupCreateInput> groups) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        httpClient.createGroups(systemId, groups);
    }

    public void deleteGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        httpClient.deleteGroup(systemId, externalGroupId);
    }

    // --- Group Attributes ---

    public List<Attribute> getGroupAttributes(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, null);
    }

    public List<Attribute> getGroupAttributes(String systemId, String externalGroupId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, keys);
    }

    public void createGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createGroupAttributes(systemId, externalGroupId, attributes);
    }

    public void replaceGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceGroupAttributes(systemId, externalGroupId, attributes);
    }

    public void replaceGroupAttributeValues(String systemId, String externalGroupId, String key, List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceGroupAttributeValues(systemId, externalGroupId, key, values);
    }

    public void deleteGroupAttribute(String systemId, String externalGroupId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteGroupAttribute(systemId, externalGroupId, key);
    }

    // --- Group Members ---

    public GroupMember.PaginatedListOf getGroupMembers(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getGroupMembers(systemId, null, null, null);
    }

    public GroupMember.PaginatedListOf getGroupMembers(String systemId, String externalGroupId, String cursor,
            Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getGroupMembers(systemId, externalGroupId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all group members for the given system across all pages.
     *
     * @param systemId the system ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<GroupMember> getGroupMembersPaginator(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.getGroupMembers(systemId, null, cursor, null)));
    }

    public void assignGroupMembers(String systemId, List<GroupMemberAssignmentInput> assignments) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(assignments, "assignments cannot be null");
        httpClient.assignGroupMembers(systemId, assignments);
    }

    public void removeGroupMembers(String systemId, String parentExternalGroupId, List<String> memberExternalUserIds,
            List<String> memberExternalGroupIds) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        httpClient.removeGroupMembers(systemId, parentExternalGroupId, memberExternalUserIds, memberExternalGroupIds);
    }

    // --- User Mappings ---

    public UserMapping.PaginatedListOf listUserMappings(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listUserMappings(systemId, null, null);
    }

    public UserMapping.PaginatedListOf listUserMappings(String systemId, String cursor, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listUserMappings(systemId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all user mappings for the given system across all pages.
     *
     * @param systemId the system ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<UserMapping> listUserMappingsPaginator(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, null)));
    }

    public UserMapping getUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMapping(systemId, externalUserId);
    }

    public void createUserMappings(String systemId, List<UserMappingCreateInput> mappings) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(mappings, "mappings cannot be null");
        httpClient.createUserMappings(systemId, mappings);
    }

    public void updateUserMapping(String systemId, String externalUserId, UserMappingReplaceInput input) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(input, "input cannot be null");
        httpClient.updateUserMapping(systemId, externalUserId, input);
    }

    public void deleteUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        httpClient.deleteUserMapping(systemId, externalUserId);
    }

    // --- User Mapping Attributes ---

    public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, null);
    }

    public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, keys);
    }

    public void createUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createUserMappingAttributes(systemId, externalUserId, attributes);
    }

    public void replaceUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceUserMappingAttributes(systemId, externalUserId, attributes);
    }

    public void replaceUserMappingAttributeValues(String systemId, String externalUserId, String key,
            List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceUserMappingAttributeValues(systemId, externalUserId, key, values);
    }

    public void deleteUserMappingAttribute(String systemId, String externalUserId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteUserMappingAttribute(systemId, externalUserId, key);
    }

    // --- Principal Users ---

    public PrincipalUserMapping.PaginatedListOf getPrincipalUserMappings(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMappings(principalUserId, null, null);
    }

    public PrincipalUserMapping.PaginatedListOf getPrincipalUserMappings(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMappings(principalUserId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all user mappings for the given principal user across all pages.
     *
     * @param principalUserId the principal user ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMapping> getPrincipalUserMappingsPaginator(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.getPrincipalUserMappings(principalUserId, cursor, null)));
    }

    public PrincipalUserMembership.PaginatedListOf getPrincipalUserMembership(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMembership(principalUserId, null, null);
    }

    public PrincipalUserMembership.PaginatedListOf getPrincipalUserMembership(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMembership(principalUserId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all memberships for the given principal user across all pages.
     *
     * @param principalUserId the principal user ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMembership> getPrincipalUserMembershipPaginator(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.getPrincipalUserMembership(principalUserId, cursor, null)));
    }

    private static <T> CursorPageableResponse<T> toPageableResponse(PaginatedList<T> page) {
        var next = page.next();
        return new CursorPageableResponse<>(page.items(), new CursorPagination(next, next != null));
    }

    /**
     * Resource handle bound to a specific system ID.
     *
     * @since 1.0.0
     */
    public static class SystemResource {

        private final NucleusHttpClient httpClient;

        private final String systemId;

        private SystemResource(NucleusHttpClient httpClient, String systemId) {
            this.httpClient = httpClient;
            this.systemId = systemId;
        }

        public String id() {
            return systemId;
        }

        public SystemOutput get() {
            return httpClient.getSystem(systemId);
        }

        public GroupOutput.PaginatedListOf listGroups() {
            return httpClient.listGroups(systemId, null, null);
        }

        public GroupOutput.PaginatedListOf listGroups(String cursor, Integer limit) {
            return httpClient.listGroups(systemId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all groups for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupOutput> listGroupsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, null)));
        }

        public GroupOutput getGroup(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroup(systemId, externalGroupId);
        }

        public void createGroups(List<GroupCreateInput> groups) {
            Objects.requireNonNull(groups, "groups cannot be null");
            httpClient.createGroups(systemId, groups);
        }

        public void deleteGroup(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            httpClient.deleteGroup(systemId, externalGroupId);
        }

        public List<Attribute> getGroupAttributes(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroupAttributes(systemId, externalGroupId, null);
        }

        public List<Attribute> getGroupAttributes(String externalGroupId, List<String> keys) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroupAttributes(systemId, externalGroupId, keys);
        }

        public CursorPageIterable<GroupOutput> listGroupsPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, limit)));
        }

        public CursorPageIterable<GroupMember> getGroupMembersPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.getGroupMembers(systemId, null, cursor, limit)));
        }

        public CursorPageIterable<UserMapping> listUserMappingsPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, limit)));
        }

        public void createGroupAttributes(String externalGroupId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.createGroupAttributes(systemId, externalGroupId, attributes);
        }

        public void replaceGroupAttributes(String externalGroupId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.replaceGroupAttributes(systemId, externalGroupId, attributes);
        }

        public void replaceGroupAttributeValues(String externalGroupId, String key, List<String> values) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(values, "values cannot be null");
            httpClient.replaceGroupAttributeValues(systemId, externalGroupId, key, values);
        }

        public void deleteGroupAttribute(String externalGroupId, String key) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            httpClient.deleteGroupAttribute(systemId, externalGroupId, key);
        }

        public GroupMember.PaginatedListOf getGroupMembers() {
            return httpClient.getGroupMembers(systemId, null, null, null);
        }

        public GroupMember.PaginatedListOf getGroupMembers(String externalGroupId, String cursor, Integer limit) {
            return httpClient.getGroupMembers(systemId, externalGroupId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all group members for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupMember> getGroupMembersPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.getGroupMembers(systemId, null, cursor, null)));
        }

        public void assignGroupMembers(List<GroupMemberAssignmentInput> assignments) {
            Objects.requireNonNull(assignments, "assignments cannot be null");
            httpClient.assignGroupMembers(systemId, assignments);
        }

        public void removeGroupMembers(String parentExternalGroupId, List<String> memberExternalUserIds,
                List<String> memberExternalGroupIds) {
            httpClient.removeGroupMembers(systemId, parentExternalGroupId, memberExternalUserIds,
                    memberExternalGroupIds);
        }

        public UserMapping.PaginatedListOf listUserMappings() {
            return httpClient.listUserMappings(systemId, null, null);
        }

        public UserMapping.PaginatedListOf listUserMappings(String cursor, Integer limit) {
            return httpClient.listUserMappings(systemId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<UserMapping> listUserMappingsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, null)));
        }

        public UserMapping getUserMapping(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMapping(systemId, externalUserId);
        }

        public void createUserMappings(List<UserMappingCreateInput> mappings) {
            Objects.requireNonNull(mappings, "mappings cannot be null");
            httpClient.createUserMappings(systemId, mappings);
        }

        public void updateUserMapping(String externalUserId, UserMappingReplaceInput input) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(input, "input cannot be null");
            httpClient.updateUserMapping(systemId, externalUserId, input);
        }

        public void deleteUserMapping(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            httpClient.deleteUserMapping(systemId, externalUserId);
        }

        public List<Attribute> getUserMappingAttributes(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMappingAttributes(systemId, externalUserId, null);
        }

        public List<Attribute> getUserMappingAttributes(String externalUserId, List<String> keys) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMappingAttributes(systemId, externalUserId, keys);
        }

        public void createUserMappingAttributes(String externalUserId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.createUserMappingAttributes(systemId, externalUserId, attributes);
        }

        public void replaceUserMappingAttributes(String externalUserId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.replaceUserMappingAttributes(systemId, externalUserId, attributes);
        }

        public void replaceUserMappingAttributeValues(String externalUserId, String key, List<String> values) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(values, "values cannot be null");
            httpClient.replaceUserMappingAttributeValues(systemId, externalUserId, key, values);
        }

        public void deleteUserMappingAttribute(String externalUserId, String key) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            httpClient.deleteUserMappingAttribute(systemId, externalUserId, key);
        }
    }

    /**
     * Resource handle bound to a specific principal user ID.
     *
     * @since 1.0.0
     */
    public static class PrincipalUserResource {

        private final NucleusHttpClient httpClient;

        private final String principalUserId;

        private PrincipalUserResource(NucleusHttpClient httpClient, String principalUserId) {
            this.httpClient = httpClient;
            this.principalUserId = principalUserId;
        }

        public String id() {
            return principalUserId;
        }

        public PrincipalUserMapping.PaginatedListOf getUserMappings() {
            return httpClient.getPrincipalUserMappings(principalUserId, null, null);
        }

        public PrincipalUserMapping.PaginatedListOf getUserMappings(String cursor, Integer limit) {
            return httpClient.getPrincipalUserMappings(principalUserId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMapping> getUserMappingsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.getPrincipalUserMappings(principalUserId, cursor, null)));
        }

        public PrincipalUserMembership.PaginatedListOf getMembership() {
            return httpClient.getPrincipalUserMembership(principalUserId, null, null);
        }

        public PrincipalUserMembership.PaginatedListOf getMembership(String cursor, Integer limit) {
            return httpClient.getPrincipalUserMembership(principalUserId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all memberships for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMembership> getMembershipPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.getPrincipalUserMembership(principalUserId, cursor, null)));
        }
    }
}
