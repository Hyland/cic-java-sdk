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
import java.util.UUID;

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
    public SystemResource system(UUID systemId) {
        return new SystemResource(httpClient, Objects.requireNonNull(systemId, "systemId cannot be null"));
    }

    /**
     * Returns a {@link PrincipalUserResource} handle bound to the given principal user ID.
     *
     * @param principalUserId the principal user ID
     * @return the principal user resource handle
     * @throws NullPointerException if principalUserId is null
     */
    public PrincipalUserResource principalUser(UUID principalUserId) {
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

    public SystemOutput getSystem(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getSystem(systemId);
    }

    // --- Groups ---

    public GroupOutput.PaginatedListOf listGroups(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroups(systemId, null, null);
    }

    public GroupOutput.PaginatedListOf listGroups(UUID systemId, String cursor, Integer limit) {
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
    public CursorPageIterable<GroupOutput> listGroupsPaginator(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, null)));
    }

    public GroupOutput getGroup(UUID systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroup(systemId, externalGroupId);
    }

    public void createGroups(UUID systemId, List<GroupCreateInput> groups) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        httpClient.createGroups(systemId, groups);
    }

    public void deleteGroup(UUID systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        httpClient.deleteGroup(systemId, externalGroupId);
    }

    // --- Group Attributes ---

    public List<Attribute> getGroupAttributes(UUID systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, null);
    }

    public List<Attribute> getGroupAttributes(UUID systemId, String externalGroupId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, keys);
    }

    public void createGroupAttributes(UUID systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createGroupAttributes(systemId, externalGroupId, attributes);
    }

    public void replaceGroupAttributes(UUID systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceGroupAttributes(systemId, externalGroupId, attributes);
    }

    public void replaceGroupAttributeValues(UUID systemId, String externalGroupId, String key, List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceGroupAttributeValues(systemId, externalGroupId, key, values);
    }

    public void deleteGroupAttribute(UUID systemId, String externalGroupId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteGroupAttribute(systemId, externalGroupId, key);
    }

    // --- Group Members ---

    public GroupMember.PaginatedListOf getGroupMembers(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getGroupMembers(systemId, null, null, null);
    }

    public GroupMember.PaginatedListOf getGroupMembers(UUID systemId, String externalGroupId, String cursor,
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
    public CursorPageIterable<GroupMember> getGroupMembersPaginator(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.getGroupMembers(systemId, null, cursor, null)));
    }

    public void assignGroupMembers(UUID systemId, List<GroupMemberAssignmentInput> assignments) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(assignments, "assignments cannot be null");
        httpClient.assignGroupMembers(systemId, assignments);
    }

    public void removeGroupMembers(UUID systemId, String parentExternalGroupId, List<String> memberExternalUserIds,
            List<String> memberExternalGroupIds) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        httpClient.removeGroupMembers(systemId, parentExternalGroupId, memberExternalUserIds, memberExternalGroupIds);
    }

    // --- User Mappings ---

    public UserMapping.PaginatedListOf listUserMappings(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listUserMappings(systemId, null, null);
    }

    public UserMapping.PaginatedListOf listUserMappings(UUID systemId, String cursor, Integer limit) {
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
    public CursorPageIterable<UserMapping> listUserMappingsPaginator(UUID systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, null)));
    }

    public UserMapping getUserMapping(UUID systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMapping(systemId, externalUserId);
    }

    public void createUserMappings(UUID systemId, List<UserMappingCreateInput> mappings) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(mappings, "mappings cannot be null");
        httpClient.createUserMappings(systemId, mappings);
    }

    public void updateUserMapping(UUID systemId, String externalUserId, UserMappingReplaceInput input) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(input, "input cannot be null");
        httpClient.updateUserMapping(systemId, externalUserId, input);
    }

    public void deleteUserMapping(UUID systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        httpClient.deleteUserMapping(systemId, externalUserId);
    }

    // --- User Mapping Attributes ---

    public List<Attribute> getUserMappingAttributes(UUID systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, null);
    }

    public List<Attribute> getUserMappingAttributes(UUID systemId, String externalUserId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, keys);
    }

    public void createUserMappingAttributes(UUID systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createUserMappingAttributes(systemId, externalUserId, attributes);
    }

    public void replaceUserMappingAttributes(UUID systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceUserMappingAttributes(systemId, externalUserId, attributes);
    }

    public void replaceUserMappingAttributeValues(UUID systemId, String externalUserId, String key,
            List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceUserMappingAttributeValues(systemId, externalUserId, key, values);
    }

    public void deleteUserMappingAttribute(UUID systemId, String externalUserId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteUserMappingAttribute(systemId, externalUserId, key);
    }

    // --- Principal Users ---

    public PrincipalUserMapping.PaginatedListOf getPrincipalUserMappings(UUID principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMappings(principalUserId, null, null);
    }

    public PrincipalUserMapping.PaginatedListOf getPrincipalUserMappings(UUID principalUserId, String cursor,
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
    public CursorPageIterable<PrincipalUserMapping> getPrincipalUserMappingsPaginator(UUID principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.getPrincipalUserMappings(principalUserId, cursor, null)));
    }

    public PrincipalUserMembership.PaginatedListOf getPrincipalUserMembership(UUID principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.getPrincipalUserMembership(principalUserId, null, null);
    }

    public PrincipalUserMembership.PaginatedListOf getPrincipalUserMembership(UUID principalUserId, String cursor,
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
    public CursorPageIterable<PrincipalUserMembership> getPrincipalUserMembershipPaginator(UUID principalUserId) {
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

        private final UUID systemId;

        private SystemResource(NucleusHttpClient httpClient, UUID systemId) {
            this.httpClient = httpClient;
            this.systemId = systemId;
        }

        public UUID id() {
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

        private final UUID principalUserId;

        private PrincipalUserResource(NucleusHttpClient httpClient, UUID principalUserId) {
            this.httpClient = httpClient;
            this.principalUserId = principalUserId;
        }

        public UUID id() {
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
