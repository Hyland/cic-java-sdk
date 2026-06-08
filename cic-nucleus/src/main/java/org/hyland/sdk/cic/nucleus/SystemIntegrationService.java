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
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupMemberPage;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.GroupOutputPage;
import org.hyland.sdk.cic.nucleus.object.ListGroupMembersRequest;
import org.hyland.sdk.cic.nucleus.object.ListGroupsRequest;
import org.hyland.sdk.cic.nucleus.object.ListPrincipalUserMappingsRequest;
import org.hyland.sdk.cic.nucleus.object.ListPrincipalUserMembershipsRequest;
import org.hyland.sdk.cic.nucleus.object.ListSystemsRequest;
import org.hyland.sdk.cic.nucleus.object.ListUserMappingsRequest;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMappingPage;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembershipPage;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.SystemOutputPage;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingPage;
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

    /**
     * Lists all systems.
     *
     * @return the first page of systems
     * @throws CICSdkException if the request fails
     */
    public SystemOutputPage listSystems() {
        return httpClient.listSystems(null, null);
    }

    /**
     * Lists systems using a builder consumer to specify optional pagination parameters.
     *
     * @param consumer configures optional parameters (cursor, limit)
     * @return the requested page of systems
     * @throws NullPointerException if consumer is null
     * @throws CICSdkException if the request fails
     */
    public SystemOutputPage listSystems(Consumer<ListSystemsRequest.Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        var builder = ListSystemsRequest.builder();
        consumer.accept(builder);
        var request = builder.build();
        return httpClient.listSystems(request.cursor(), request.limit());
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all systems across all pages.
     *
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<SystemOutput> listSystemsPaginator() {
        return new CursorPageIterable<>(cursor -> httpClient.listSystems(cursor, null));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all systems across all pages, using a builder consumer to specify
     * optional pagination parameters.
     * <p>
     * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
     *
     * @param consumer configures optional parameters (limit)
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if consumer is null
     */
    public CursorPageIterable<SystemOutput> listSystemsPaginator(Consumer<ListSystemsRequest.Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        var builder = ListSystemsRequest.builder();
        consumer.accept(builder);
        var request = builder.build();
        return new CursorPageIterable<>(cursor -> httpClient.listSystems(cursor, request.limit()));
    }

    /**
     * Gets a system by ID.
     *
     * @param systemId the system ID
     * @return the system
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public SystemOutput getSystem(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.getSystem(systemId);
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

        /**
         * Returns the system ID this handle is bound to.
         *
         * @return the system ID
         */
        public String id() {
            return systemId;
        }

        /**
         * Gets the system.
         *
         * @return the system
         * @throws CICSdkException if the request fails
         */
        public SystemOutput get() {
            return httpClient.getSystem(systemId);
        }

        /**
         * Lists all groups for this system.
         *
         * @return the first page of groups
         * @throws CICSdkException if the request fails
         */
        public GroupOutputPage listGroups() {
            return httpClient.listGroups(systemId, null, null);
        }

        /**
         * Lists groups for this system using a builder consumer to specify optional pagination parameters.
         *
         * @param consumer configures optional parameters (cursor, limit)
         * @return the requested page of groups
         * @throws NullPointerException if consumer is null
         * @throws CICSdkException if the request fails
         */
        public GroupOutputPage listGroups(Consumer<ListGroupsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListGroupsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listGroups(systemId, request.cursor(), request.limit());
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all groups for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupOutput> listGroupsPaginator() {
            return new CursorPageIterable<>(cursor -> httpClient.listGroups(systemId, cursor, null));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all groups for this system across all pages, using a builder
         * consumer to specify optional pagination parameters.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (limit)
         * @return an iterable that fetches pages on demand
         * @throws NullPointerException if consumer is null
         */
        public CursorPageIterable<GroupOutput> listGroupsPaginator(Consumer<ListGroupsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListGroupsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(cursor -> httpClient.listGroups(systemId, cursor, request.limit()));
        }

        /**
         * Gets a group by external group ID.
         *
         * @param externalGroupId the external group ID
         * @return the group
         * @throws NullPointerException if externalGroupId is null
         * @throws CICSdkException if the request fails
         */
        public GroupOutput getGroup(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroup(systemId, externalGroupId);
        }

        /**
         * Creates groups in this system.
         *
         * @param groups the groups to create
         * @throws NullPointerException if groups is null
         * @throws CICSdkException if the request fails
         */
        public void createGroups(List<GroupCreateInput> groups) {
            Objects.requireNonNull(groups, "groups cannot be null");
            httpClient.createGroups(systemId, groups);
        }

        /**
         * Deletes a group from this system.
         *
         * @param externalGroupId the external group ID
         * @throws NullPointerException if externalGroupId is null
         * @throws CICSdkException if the request fails
         */
        public void deleteGroup(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            httpClient.deleteGroup(systemId, externalGroupId);
        }

        /**
         * Gets all attributes for the given group.
         *
         * @param externalGroupId the external group ID
         * @return the group attributes
         * @throws NullPointerException if externalGroupId is null
         * @throws CICSdkException if the request fails
         */
        public List<Attribute> getGroupAttributes(String externalGroupId) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroupAttributes(systemId, externalGroupId, null);
        }

        /**
         * Gets attributes for the given group, filtered by key.
         *
         * @param externalGroupId the external group ID
         * @param keys the attribute keys to retrieve
         * @return the group attributes
         * @throws NullPointerException if externalGroupId is null
         * @throws CICSdkException if the request fails
         */
        public List<Attribute> getGroupAttributes(String externalGroupId, List<String> keys) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            return httpClient.getGroupAttributes(systemId, externalGroupId, keys);
        }

        /**
         * Creates attributes for the given group.
         *
         * @param externalGroupId the external group ID
         * @param attributes the attributes to create
         * @throws NullPointerException if externalGroupId or attributes is null
         * @throws CICSdkException if the request fails
         */
        public void createGroupAttributes(String externalGroupId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.createGroupAttributes(systemId, externalGroupId, attributes);
        }

        /**
         * Replaces all attributes for the given group.
         *
         * @param externalGroupId the external group ID
         * @param attributes the replacement attributes
         * @throws NullPointerException if externalGroupId or attributes is null
         * @throws CICSdkException if the request fails
         */
        public void replaceGroupAttributes(String externalGroupId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.replaceGroupAttributes(systemId, externalGroupId, attributes);
        }

        /**
         * Replaces the values of a single attribute for the given group.
         *
         * @param externalGroupId the external group ID
         * @param key the attribute key
         * @param values the replacement values
         * @throws NullPointerException if externalGroupId, key, or values is null
         * @throws CICSdkException if the request fails
         */
        public void replaceGroupAttributeValues(String externalGroupId, String key, List<String> values) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(values, "values cannot be null");
            httpClient.replaceGroupAttributeValues(systemId, externalGroupId, key, values);
        }

        /**
         * Deletes an attribute from the given group.
         *
         * @param externalGroupId the external group ID
         * @param key the attribute key
         * @throws NullPointerException if externalGroupId or key is null
         * @throws CICSdkException if the request fails
         */
        public void deleteGroupAttribute(String externalGroupId, String key) {
            Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            httpClient.deleteGroupAttribute(systemId, externalGroupId, key);
        }

        /**
         * Lists all group members for this system.
         *
         * @return the first page of group members
         * @throws CICSdkException if the request fails
         */
        public GroupMemberPage listGroupMembers() {
            return httpClient.listGroupMembers(systemId, null, null, null);
        }

        /**
         * Lists group members for this system using a builder consumer to specify optional filter and pagination
         * parameters.
         *
         * @param consumer configures optional parameters (externalGroupId, cursor, limit)
         * @return the requested page of group members
         * @throws NullPointerException if consumer is null
         * @throws CICSdkException if the request fails
         */
        public GroupMemberPage listGroupMembers(Consumer<ListGroupMembersRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListGroupMembersRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listGroupMembers(systemId, request.externalGroupId(), request.cursor(), request.limit());
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all group members for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupMember> listGroupMembersPaginator() {
            return new CursorPageIterable<>(cursor -> httpClient.listGroupMembers(systemId, null, cursor, null));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all group members for this system across all pages, using a
         * builder consumer to specify optional filter and pagination parameters.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (externalGroupId, limit)
         * @return an iterable that fetches pages on demand
         * @throws NullPointerException if consumer is null
         */
        public CursorPageIterable<GroupMember> listGroupMembersPaginator(
                Consumer<ListGroupMembersRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListGroupMembersRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(cursor -> httpClient.listGroupMembers(systemId, request.externalGroupId(),
                    cursor, request.limit()));
        }

        /**
         * Assigns members to groups in this system.
         *
         * @param assignments the member assignments
         * @throws NullPointerException if assignments is null
         * @throws CICSdkException if the request fails
         */
        public void assignGroupMembers(List<GroupMemberAssignmentInput> assignments) {
            Objects.requireNonNull(assignments, "assignments cannot be null");
            httpClient.assignGroupMembers(systemId, assignments);
        }

        /**
         * Removes members from a group in this system.
         *
         * @param parentExternalGroupId the external group ID from which members are removed
         * @param memberExternalUserIds external user IDs to remove, or {@code null}
         * @param memberExternalGroupIds external group IDs to remove, or {@code null}
         * @throws CICSdkException if the request fails
         */
        public void removeGroupMembers(String parentExternalGroupId, List<String> memberExternalUserIds,
                List<String> memberExternalGroupIds) {
            httpClient.removeGroupMembers(systemId, parentExternalGroupId, memberExternalUserIds,
                    memberExternalGroupIds);
        }

        /**
         * Lists all user mappings for this system.
         *
         * @return the first page of user mappings
         * @throws CICSdkException if the request fails
         */
        public UserMappingPage listUserMappings() {
            return httpClient.listUserMappings(systemId, null, null);
        }

        /**
         * Lists user mappings for this system using a builder consumer to specify optional pagination parameters.
         *
         * @param consumer configures optional parameters (cursor, limit)
         * @return the requested page of user mappings
         * @throws NullPointerException if consumer is null
         * @throws CICSdkException if the request fails
         */
        public UserMappingPage listUserMappings(Consumer<ListUserMappingsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListUserMappingsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listUserMappings(systemId, request.cursor(), request.limit());
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<UserMapping> listUserMappingsPaginator() {
            return new CursorPageIterable<>(cursor -> httpClient.listUserMappings(systemId, cursor, null));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this system across all pages, using a
         * builder consumer to specify optional pagination parameters.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (limit)
         * @return an iterable that fetches pages on demand
         * @throws NullPointerException if consumer is null
         */
        public CursorPageIterable<UserMapping> listUserMappingsPaginator(
                Consumer<ListUserMappingsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListUserMappingsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(cursor -> httpClient.listUserMappings(systemId, cursor, request.limit()));
        }

        /**
         * Gets a user mapping by external user ID.
         *
         * @param externalUserId the external user ID
         * @return the user mapping
         * @throws NullPointerException if externalUserId is null
         * @throws CICSdkException if the request fails
         */
        public UserMapping getUserMapping(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMapping(systemId, externalUserId);
        }

        /**
         * Creates user mappings in this system.
         *
         * @param mappings the mappings to create
         * @throws NullPointerException if mappings is null
         * @throws CICSdkException if the request fails
         */
        public void createUserMappings(List<UserMappingCreateInput> mappings) {
            Objects.requireNonNull(mappings, "mappings cannot be null");
            httpClient.createUserMappings(systemId, mappings);
        }

        /**
         * Updates a user mapping in this system.
         *
         * @param externalUserId the external user ID
         * @param input the replacement mapping input
         * @throws NullPointerException if externalUserId or input is null
         * @throws CICSdkException if the request fails
         */
        public void updateUserMapping(String externalUserId, UserMappingReplaceInput input) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(input, "input cannot be null");
            httpClient.updateUserMapping(systemId, externalUserId, input);
        }

        /**
         * Deletes a user mapping from this system.
         *
         * @param externalUserId the external user ID
         * @throws NullPointerException if externalUserId is null
         * @throws CICSdkException if the request fails
         */
        public void deleteUserMapping(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            httpClient.deleteUserMapping(systemId, externalUserId);
        }

        /**
         * Gets all attributes for the given user mapping.
         *
         * @param externalUserId the external user ID
         * @return the user mapping attributes
         * @throws NullPointerException if externalUserId is null
         * @throws CICSdkException if the request fails
         */
        public List<Attribute> getUserMappingAttributes(String externalUserId) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMappingAttributes(systemId, externalUserId, null);
        }

        /**
         * Gets attributes for the given user mapping, filtered by key.
         *
         * @param externalUserId the external user ID
         * @param keys the attribute keys to retrieve
         * @return the user mapping attributes
         * @throws NullPointerException if externalUserId is null
         * @throws CICSdkException if the request fails
         */
        public List<Attribute> getUserMappingAttributes(String externalUserId, List<String> keys) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            return httpClient.getUserMappingAttributes(systemId, externalUserId, keys);
        }

        /**
         * Creates attributes for the given user mapping.
         *
         * @param externalUserId the external user ID
         * @param attributes the attributes to create
         * @throws NullPointerException if externalUserId or attributes is null
         * @throws CICSdkException if the request fails
         */
        public void createUserMappingAttributes(String externalUserId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.createUserMappingAttributes(systemId, externalUserId, attributes);
        }

        /**
         * Replaces all attributes for the given user mapping.
         *
         * @param externalUserId the external user ID
         * @param attributes the replacement attributes
         * @throws NullPointerException if externalUserId or attributes is null
         * @throws CICSdkException if the request fails
         */
        public void replaceUserMappingAttributes(String externalUserId, List<AttributeInput> attributes) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(attributes, "attributes cannot be null");
            httpClient.replaceUserMappingAttributes(systemId, externalUserId, attributes);
        }

        /**
         * Replaces the values of a single attribute for the given user mapping.
         *
         * @param externalUserId the external user ID
         * @param key the attribute key
         * @param values the replacement values
         * @throws NullPointerException if externalUserId, key, or values is null
         * @throws CICSdkException if the request fails
         */
        public void replaceUserMappingAttributeValues(String externalUserId, String key, List<String> values) {
            Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
            Objects.requireNonNull(key, "key cannot be null");
            Objects.requireNonNull(values, "values cannot be null");
            httpClient.replaceUserMappingAttributeValues(systemId, externalUserId, key, values);
        }

        /**
         * Deletes an attribute from the given user mapping.
         *
         * @param externalUserId the external user ID
         * @param key the attribute key
         * @throws NullPointerException if externalUserId or key is null
         * @throws CICSdkException if the request fails
         */
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

        /**
         * Returns the principal user ID this handle is bound to.
         *
         * @return the principal user ID
         */
        public String id() {
            return principalUserId;
        }

        /**
         * Lists all user mappings for this principal user.
         *
         * @return the first page of user mappings
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMappingPage listUserMappings() {
            return httpClient.listPrincipalUserMappings(principalUserId, null, null);
        }

        /**
         * Lists user mappings for this principal user using a builder consumer to specify optional pagination
         * parameters.
         *
         * @param consumer configures optional parameters (cursor, limit)
         * @return the requested page of user mappings
         * @throws NullPointerException if consumer is null
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMappingPage listUserMappings(Consumer<ListPrincipalUserMappingsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListPrincipalUserMappingsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listPrincipalUserMappings(principalUserId, request.cursor(), request.limit());
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMapping> listUserMappingsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> httpClient.listPrincipalUserMappings(principalUserId, cursor, null));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this principal user across all pages,
         * using a builder consumer to specify optional pagination parameters.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (limit)
         * @return an iterable that fetches pages on demand
         * @throws NullPointerException if consumer is null
         */
        public CursorPageIterable<PrincipalUserMapping> listUserMappingsPaginator(
                Consumer<ListPrincipalUserMappingsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListPrincipalUserMappingsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(
                    cursor -> httpClient.listPrincipalUserMappings(principalUserId, cursor, request.limit()));
        }

        /**
         * Lists all memberships for this principal user.
         *
         * @return the first page of memberships
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMembershipPage listMemberships() {
            return httpClient.listPrincipalUserMemberships(principalUserId, null, null);
        }

        /**
         * Lists memberships for this principal user using a builder consumer to specify optional pagination parameters.
         *
         * @param consumer configures optional parameters (cursor, limit)
         * @return the requested page of memberships
         * @throws NullPointerException if consumer is null
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMembershipPage listMemberships(
                Consumer<ListPrincipalUserMembershipsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListPrincipalUserMembershipsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listPrincipalUserMemberships(principalUserId, request.cursor(), request.limit());
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all memberships for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMembership> listMembershipsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> httpClient.listPrincipalUserMemberships(principalUserId, cursor, null));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all memberships for this principal user across all pages,
         * using a builder consumer to specify optional pagination parameters.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (limit)
         * @return an iterable that fetches pages on demand
         * @throws NullPointerException if consumer is null
         */
        public CursorPageIterable<PrincipalUserMembership> listMembershipsPaginator(
                Consumer<ListPrincipalUserMembershipsRequest.Builder> consumer) {
            Objects.requireNonNull(consumer, "consumer cannot be null");
            var builder = ListPrincipalUserMembershipsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(
                    cursor -> httpClient.listPrincipalUserMemberships(principalUserId, cursor, request.limit()));
        }
    }
}
