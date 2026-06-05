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

import org.hyland.sdk.cic.http.client.CICSdkException;
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

    /**
     * Lists all systems.
     *
     * @return the first page of systems
     * @throws CICSdkException if the request fails
     */
    public SystemOutput.PaginatedListOf listSystems() {
        return httpClient.listSystems(null, null);
    }

    /**
     * Lists systems for the given page.
     *
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of systems
     * @throws CICSdkException if the request fails
     */
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

    /**
     * Returns a lazily-fetching {@link Iterable} over all systems across all pages, using the given page size.
     *
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<SystemOutput> listSystemsPaginator(Integer limit) {
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listSystems(cursor, limit)));
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

    // --- Groups ---

    /**
     * Lists all groups for the given system.
     *
     * @param systemId the system ID
     * @return the first page of groups
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public GroupOutput.PaginatedListOf listGroups(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroups(systemId, null, null);
    }

    /**
     * Lists groups for the given system and page.
     *
     * @param systemId the system ID
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of groups
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
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

    /**
     * Returns a lazily-fetching {@link Iterable} over all groups for the given system across all pages, using the given
     * page size.
     *
     * @param systemId the system ID
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<GroupOutput> listGroupsPaginator(String systemId, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, limit)));
    }

    /**
     * Gets a group by system ID and external group ID.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @return the group
     * @throws NullPointerException if systemId or externalGroupId is null
     * @throws CICSdkException if the request fails
     */
    public GroupOutput getGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroup(systemId, externalGroupId);
    }

    /**
     * Creates groups in the given system.
     *
     * @param systemId the system ID
     * @param groups the groups to create
     * @throws NullPointerException if systemId or groups is null
     * @throws CICSdkException if the request fails
     */
    public void createGroups(String systemId, List<GroupCreateInput> groups) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        httpClient.createGroups(systemId, groups);
    }

    /**
     * Deletes a group from the given system.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @throws NullPointerException if systemId or externalGroupId is null
     * @throws CICSdkException if the request fails
     */
    public void deleteGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        httpClient.deleteGroup(systemId, externalGroupId);
    }

    // --- Group Attributes ---

    /**
     * Gets all attributes for the given group.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @return the group attributes
     * @throws NullPointerException if systemId or externalGroupId is null
     * @throws CICSdkException if the request fails
     */
    public List<Attribute> getGroupAttributes(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, null);
    }

    /**
     * Gets attributes for the given group, filtered by key.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @param keys the attribute keys to retrieve
     * @return the group attributes
     * @throws NullPointerException if systemId or externalGroupId is null
     * @throws CICSdkException if the request fails
     */
    public List<Attribute> getGroupAttributes(String systemId, String externalGroupId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        return httpClient.getGroupAttributes(systemId, externalGroupId, keys);
    }

    /**
     * Creates attributes for the given group.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @param attributes the attributes to create
     * @throws NullPointerException if systemId, externalGroupId, or attributes is null
     * @throws CICSdkException if the request fails
     */
    public void createGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createGroupAttributes(systemId, externalGroupId, attributes);
    }

    /**
     * Replaces all attributes for the given group.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @param attributes the replacement attributes
     * @throws NullPointerException if systemId, externalGroupId, or attributes is null
     * @throws CICSdkException if the request fails
     */
    public void replaceGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceGroupAttributes(systemId, externalGroupId, attributes);
    }

    /**
     * Replaces the values of a single attribute for the given group.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @param key the attribute key
     * @param values the replacement values
     * @throws NullPointerException if systemId, externalGroupId, key, or values is null
     * @throws CICSdkException if the request fails
     */
    public void replaceGroupAttributeValues(String systemId, String externalGroupId, String key, List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceGroupAttributeValues(systemId, externalGroupId, key, values);
    }

    /**
     * Deletes an attribute from the given group.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID
     * @param key the attribute key
     * @throws NullPointerException if systemId, externalGroupId, or key is null
     * @throws CICSdkException if the request fails
     */
    public void deleteGroupAttribute(String systemId, String externalGroupId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteGroupAttribute(systemId, externalGroupId, key);
    }

    // --- Group Members ---

    /**
     * Lists all group members for the given system.
     *
     * @param systemId the system ID
     * @return the first page of group members
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public GroupMember.PaginatedListOf listGroupMembers(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroupMembers(systemId, null, null, null);
    }

    /**
     * Lists group members for the given system and page.
     *
     * @param systemId the system ID
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of group members
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public GroupMember.PaginatedListOf listGroupMembers(String systemId, String cursor, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroupMembers(systemId, null, cursor, limit);
    }

    /**
     * Lists group members for the given system, filtered by group, and page.
     *
     * @param systemId the system ID
     * @param externalGroupId the external group ID to filter by, or {@code null} for no filter
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of group members
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public GroupMember.PaginatedListOf listGroupMembers(String systemId, String externalGroupId, String cursor,
            Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listGroupMembers(systemId, externalGroupId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all group members for the given system across all pages.
     *
     * @param systemId the system ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<GroupMember> listGroupMembersPaginator(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listGroupMembers(systemId, null, cursor, null)));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all group members for the given system across all pages, using
     * the given page size.
     *
     * @param systemId the system ID
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<GroupMember> listGroupMembersPaginator(String systemId, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listGroupMembers(systemId, null, cursor, limit)));
    }

    /**
     * Assigns members to groups in the given system.
     *
     * @param systemId the system ID
     * @param assignments the member assignments
     * @throws NullPointerException if systemId or assignments is null
     * @throws CICSdkException if the request fails
     */
    public void assignGroupMembers(String systemId, List<GroupMemberAssignmentInput> assignments) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(assignments, "assignments cannot be null");
        httpClient.assignGroupMembers(systemId, assignments);
    }

    /**
     * Removes members from a group in the given system.
     *
     * @param systemId the system ID
     * @param parentExternalGroupId the external group ID from which members are removed
     * @param memberExternalUserIds external user IDs to remove, or {@code null}
     * @param memberExternalGroupIds external group IDs to remove, or {@code null}
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public void removeGroupMembers(String systemId, String parentExternalGroupId, List<String> memberExternalUserIds,
            List<String> memberExternalGroupIds) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        httpClient.removeGroupMembers(systemId, parentExternalGroupId, memberExternalUserIds, memberExternalGroupIds);
    }

    // --- User Mappings ---

    /**
     * Lists all user mappings for the given system.
     *
     * @param systemId the system ID
     * @return the first page of user mappings
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
    public UserMapping.PaginatedListOf listUserMappings(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return httpClient.listUserMappings(systemId, null, null);
    }

    /**
     * Lists user mappings for the given system and page.
     *
     * @param systemId the system ID
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of user mappings
     * @throws NullPointerException if systemId is null
     * @throws CICSdkException if the request fails
     */
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

    /**
     * Returns a lazily-fetching {@link Iterable} over all user mappings for the given system across all pages, using
     * the given page size.
     *
     * @param systemId the system ID
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if systemId is null
     */
    public CursorPageIterable<UserMapping> listUserMappingsPaginator(String systemId, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, limit)));
    }

    /**
     * Gets a user mapping by system ID and external user ID.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @return the user mapping
     * @throws NullPointerException if systemId or externalUserId is null
     * @throws CICSdkException if the request fails
     */
    public UserMapping getUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMapping(systemId, externalUserId);
    }

    /**
     * Creates user mappings in the given system.
     *
     * @param systemId the system ID
     * @param mappings the mappings to create
     * @throws NullPointerException if systemId or mappings is null
     * @throws CICSdkException if the request fails
     */
    public void createUserMappings(String systemId, List<UserMappingCreateInput> mappings) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(mappings, "mappings cannot be null");
        httpClient.createUserMappings(systemId, mappings);
    }

    /**
     * Updates a user mapping in the given system.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param input the replacement mapping input
     * @throws NullPointerException if systemId, externalUserId, or input is null
     * @throws CICSdkException if the request fails
     */
    public void updateUserMapping(String systemId, String externalUserId, UserMappingReplaceInput input) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(input, "input cannot be null");
        httpClient.updateUserMapping(systemId, externalUserId, input);
    }

    /**
     * Deletes a user mapping from the given system.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @throws NullPointerException if systemId or externalUserId is null
     * @throws CICSdkException if the request fails
     */
    public void deleteUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        httpClient.deleteUserMapping(systemId, externalUserId);
    }

    // --- User Mapping Attributes ---

    /**
     * Gets all attributes for the given user mapping.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @return the user mapping attributes
     * @throws NullPointerException if systemId or externalUserId is null
     * @throws CICSdkException if the request fails
     */
    public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, null);
    }

    /**
     * Gets attributes for the given user mapping, filtered by key.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param keys the attribute keys to retrieve
     * @return the user mapping attributes
     * @throws NullPointerException if systemId or externalUserId is null
     * @throws CICSdkException if the request fails
     */
    public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        return httpClient.getUserMappingAttributes(systemId, externalUserId, keys);
    }

    /**
     * Creates attributes for the given user mapping.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param attributes the attributes to create
     * @throws NullPointerException if systemId, externalUserId, or attributes is null
     * @throws CICSdkException if the request fails
     */
    public void createUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.createUserMappingAttributes(systemId, externalUserId, attributes);
    }

    /**
     * Replaces all attributes for the given user mapping.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param attributes the replacement attributes
     * @throws NullPointerException if systemId, externalUserId, or attributes is null
     * @throws CICSdkException if the request fails
     */
    public void replaceUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        httpClient.replaceUserMappingAttributes(systemId, externalUserId, attributes);
    }

    /**
     * Replaces the values of a single attribute for the given user mapping.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param key the attribute key
     * @param values the replacement values
     * @throws NullPointerException if systemId, externalUserId, key, or values is null
     * @throws CICSdkException if the request fails
     */
    public void replaceUserMappingAttributeValues(String systemId, String externalUserId, String key,
            List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        httpClient.replaceUserMappingAttributeValues(systemId, externalUserId, key, values);
    }

    /**
     * Deletes an attribute from the given user mapping.
     *
     * @param systemId the system ID
     * @param externalUserId the external user ID
     * @param key the attribute key
     * @throws NullPointerException if systemId, externalUserId, or key is null
     * @throws CICSdkException if the request fails
     */
    public void deleteUserMappingAttribute(String systemId, String externalUserId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        httpClient.deleteUserMappingAttribute(systemId, externalUserId, key);
    }

    // --- Principal Users ---

    /**
     * Lists all user mappings for the given principal user.
     *
     * @param principalUserId the principal user ID
     * @return the first page of principal user mappings
     * @throws NullPointerException if principalUserId is null
     * @throws CICSdkException if the request fails
     */
    public PrincipalUserMapping.PaginatedListOf listPrincipalUserMappings(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.listPrincipalUserMappings(principalUserId, null, null);
    }

    /**
     * Lists user mappings for the given principal user and page.
     *
     * @param principalUserId the principal user ID
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of principal user mappings
     * @throws NullPointerException if principalUserId is null
     * @throws CICSdkException if the request fails
     */
    public PrincipalUserMapping.PaginatedListOf listPrincipalUserMappings(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.listPrincipalUserMappings(principalUserId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all user mappings for the given principal user across all pages.
     *
     * @param principalUserId the principal user ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMapping> listPrincipalUserMappingsPaginator(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listPrincipalUserMappings(principalUserId, cursor, null)));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all user mappings for the given principal user across all pages,
     * using the given page size.
     *
     * @param principalUserId the principal user ID
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMapping> listPrincipalUserMappingsPaginator(String principalUserId,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listPrincipalUserMappings(principalUserId, cursor, limit)));
    }

    /**
     * Lists all memberships for the given principal user.
     *
     * @param principalUserId the principal user ID
     * @return the first page of memberships
     * @throws NullPointerException if principalUserId is null
     * @throws CICSdkException if the request fails
     */
    public PrincipalUserMembership.PaginatedListOf listPrincipalUserMemberships(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.listPrincipalUserMemberships(principalUserId, null, null);
    }

    /**
     * Lists memberships for the given principal user and page.
     *
     * @param principalUserId the principal user ID
     * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
     * @param limit the maximum number of items to return, or {@code null} for the server default
     * @return the requested page of memberships
     * @throws NullPointerException if principalUserId is null
     * @throws CICSdkException if the request fails
     */
    public PrincipalUserMembership.PaginatedListOf listPrincipalUserMemberships(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return httpClient.listPrincipalUserMemberships(principalUserId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all memberships for the given principal user across all pages.
     *
     * @param principalUserId the principal user ID
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMembership> listPrincipalUserMembershipsPaginator(String principalUserId) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listPrincipalUserMemberships(principalUserId, cursor, null)));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all memberships for the given principal user across all pages,
     * using the given page size.
     *
     * @param principalUserId the principal user ID
     * @param limit the page size, or {@code null} for the server default
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if principalUserId is null
     */
    public CursorPageIterable<PrincipalUserMembership> listPrincipalUserMembershipsPaginator(String principalUserId,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        return new CursorPageIterable<>(
                cursor -> toPageableResponse(httpClient.listPrincipalUserMemberships(principalUserId, cursor, limit)));
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
        public GroupOutput.PaginatedListOf listGroups() {
            return httpClient.listGroups(systemId, null, null);
        }

        /**
         * Lists groups for this system and page.
         *
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of groups
         * @throws CICSdkException if the request fails
         */
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

        /**
         * Returns a lazily-fetching {@link Iterable} over all groups for this system across all pages, using the given
         * page size.
         *
         * @param limit the page size, or {@code null} for the server default
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupOutput> listGroupsPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listGroups(systemId, cursor, limit)));
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
        public GroupMember.PaginatedListOf listGroupMembers() {
            return httpClient.listGroupMembers(systemId, null, null, null);
        }

        /**
         * Lists group members for this system and page.
         *
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of group members
         * @throws CICSdkException if the request fails
         */
        public GroupMember.PaginatedListOf listGroupMembers(String cursor, Integer limit) {
            return httpClient.listGroupMembers(systemId, null, cursor, limit);
        }

        /**
         * Lists group members for this system, filtered by group, and page.
         *
         * @param externalGroupId the external group ID to filter by, or {@code null} for no filter
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of group members
         * @throws CICSdkException if the request fails
         */
        public GroupMember.PaginatedListOf listGroupMembers(String externalGroupId, String cursor, Integer limit) {
            return httpClient.listGroupMembers(systemId, externalGroupId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all group members for this system across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupMember> listGroupMembersPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listGroupMembers(systemId, null, cursor, null)));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all group members for this system across all pages, using the
         * given page size.
         *
         * @param limit the page size, or {@code null} for the server default
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<GroupMember> listGroupMembersPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listGroupMembers(systemId, null, cursor, limit)));
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
        public UserMapping.PaginatedListOf listUserMappings() {
            return httpClient.listUserMappings(systemId, null, null);
        }

        /**
         * Lists user mappings for this system and page.
         *
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of user mappings
         * @throws CICSdkException if the request fails
         */
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

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this system across all pages, using the
         * given page size.
         *
         * @param limit the page size, or {@code null} for the server default
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<UserMapping> listUserMappingsPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listUserMappings(systemId, cursor, limit)));
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
        public PrincipalUserMapping.PaginatedListOf listUserMappings() {
            return httpClient.listPrincipalUserMappings(principalUserId, null, null);
        }

        /**
         * Lists user mappings for this principal user and page.
         *
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of user mappings
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMapping.PaginatedListOf listUserMappings(String cursor, Integer limit) {
            return httpClient.listPrincipalUserMappings(principalUserId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMapping> listUserMappingsPaginator() {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listPrincipalUserMappings(principalUserId, cursor, null)));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all user mappings for this principal user across all pages,
         * using the given page size.
         *
         * @param limit the page size, or {@code null} for the server default
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMapping> listUserMappingsPaginator(Integer limit) {
            return new CursorPageIterable<>(
                    cursor -> toPageableResponse(httpClient.listPrincipalUserMappings(principalUserId, cursor, limit)));
        }

        /**
         * Lists all memberships for this principal user.
         *
         * @return the first page of memberships
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMembership.PaginatedListOf listMemberships() {
            return httpClient.listPrincipalUserMemberships(principalUserId, null, null);
        }

        /**
         * Lists memberships for this principal user and page.
         *
         * @param cursor the pagination cursor from a previous response, or {@code null} for the first page
         * @param limit the maximum number of items to return, or {@code null} for the server default
         * @return the requested page of memberships
         * @throws CICSdkException if the request fails
         */
        public PrincipalUserMembership.PaginatedListOf listMemberships(String cursor, Integer limit) {
            return httpClient.listPrincipalUserMemberships(principalUserId, cursor, limit);
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all memberships for this principal user across all pages.
         *
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMembership> listMembershipsPaginator() {
            return new CursorPageIterable<>(cursor -> toPageableResponse(
                    httpClient.listPrincipalUserMemberships(principalUserId, cursor, null)));
        }

        /**
         * Returns a lazily-fetching {@link Iterable} over all memberships for this principal user across all pages,
         * using the given page size.
         *
         * @param limit the page size, or {@code null} for the server default
         * @return an iterable that fetches pages on demand
         */
        public CursorPageIterable<PrincipalUserMembership> listMembershipsPaginator(Integer limit) {
            return new CursorPageIterable<>(cursor -> toPageableResponse(
                    httpClient.listPrincipalUserMemberships(principalUserId, cursor, limit)));
        }
    }
}
