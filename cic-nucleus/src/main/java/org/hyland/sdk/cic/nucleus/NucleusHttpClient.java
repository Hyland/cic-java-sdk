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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.DELETE;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.PUT;

import java.util.List;
import java.util.UUID;

import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * HTTP client for interacting with the CIC System Integrations API.
 *
 * @since 1.0.0
 */
public class NucleusHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String SYSTEMS_PATH = "/system-integrations/systems";

    private static final String PRINCIPAL_USERS_PATH = "/system-integrations/principal-users";

    private static final String USERS_PATH = "/api/users";

    protected NucleusHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    public SystemOutput.PaginatedListOf listSystems(String cursor, Integer limit) {
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH);
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), SystemOutput.PaginatedListOf.class);
    }

    public SystemOutput getSystem(UUID systemId) {
        var request = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId).build();
        return sendThenMapAs(request, SystemOutput.class);
    }

    public GroupOutput.PaginatedListOf listGroups(UUID systemId, String cursor, Integer limit) {
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId + "/groups");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), GroupOutput.PaginatedListOf.class);
    }

    public GroupOutput getGroup(UUID systemId, String externalGroupId) {
        var request = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId).build();
        return sendThenMapAs(request, GroupOutput.class);
    }

    public void createGroups(UUID systemId, List<GroupCreateInput> groups) {
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + systemId + "/groups")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(groups))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to create groups, HTTP response returned with status code: " + response.statusCode());
        }
    }

    public void deleteGroup(UUID systemId, String externalGroupId) {
        var request = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId).build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete group, HTTP response returned with status code: " + response.statusCode());
        }
    }

    public List<Attribute> getGroupAttributes(UUID systemId, String externalGroupId, List<String> keys) {
        var requestBuilder = this.requestBuilder(GET,
                SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId + "/attributes");
        if (keys != null) {
            keys.forEach(key -> requestBuilder.queryParameter("key", key));
        }
        return sendThenMapAs(requestBuilder.build(), Attribute.ListOf.class);
    }

    public void createGroupAttributes(UUID systemId, String externalGroupId, List<AttributeInput> attributes) {
        var request = this.requestBuilder(POST,
                SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            ErrorUtils.throwException(response,
                    "Failed to create group attributes, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void replaceGroupAttributes(UUID systemId, String externalGroupId, List<AttributeInput> attributes) {
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to replace group attributes, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void replaceGroupAttributeValues(UUID systemId, String externalGroupId, String key, List<String> values) {
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId + "/attributes/" + key)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(values))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to replace group attribute values, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void deleteGroupAttribute(UUID systemId, String externalGroupId, String key) {
        var request = this.requestBuilder(DELETE,
                SYSTEMS_PATH + "/" + systemId + "/groups/" + externalGroupId + "/attributes/" + key).build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete group attribute, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public GroupMember.PaginatedListOf getGroupMembers(UUID systemId, String externalGroupId, String cursor,
            Integer limit) {
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId + "/group-members");
        if (externalGroupId != null) {
            requestBuilder.queryParameter("externalGroupId", externalGroupId);
        }
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), GroupMember.PaginatedListOf.class);
    }

    public void assignGroupMembers(UUID systemId, List<GroupMemberAssignmentInput> assignments) {
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + systemId + "/group-members")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(assignments))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to assign group members, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void removeGroupMembers(UUID systemId, String parentExternalGroupId, List<String> memberExternalUserIds,
            List<String> memberExternalGroupIds) {
        var requestBuilder = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + systemId + "/group-members");
        if (parentExternalGroupId != null) {
            requestBuilder.queryParameter("parentExternalGroupId", parentExternalGroupId);
        }
        if (memberExternalUserIds != null) {
            memberExternalUserIds.forEach(id -> requestBuilder.queryParameter("memberExternalUserIds", id));
        }
        if (memberExternalGroupIds != null) {
            memberExternalGroupIds.forEach(id -> requestBuilder.queryParameter("memberExternalGroupIds", id));
        }
        var response = sendThenReadAsString(requestBuilder.build());
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to remove group members, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public UserMapping.PaginatedListOf listUserMappings(UUID systemId, String cursor, Integer limit) {
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId + "/user-mappings");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), UserMapping.PaginatedListOf.class);
    }

    public UserMapping getUserMapping(UUID systemId, String externalUserId) {
        var request = this.requestBuilder(GET, SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId)
                          .build();
        return sendThenMapAs(request, UserMapping.class);
    }

    public void createUserMappings(UUID systemId, List<UserMappingCreateInput> mappings) {
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + systemId + "/user-mappings")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(mappings))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to create user mappings, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void updateUserMapping(UUID systemId, String externalUserId, UserMappingReplaceInput input) {
        var request = this.requestBuilder(PUT, SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(input))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204 && response.statusCode() != 200) {
            ErrorUtils.throwException(response,
                    "Failed to update user mapping, HTTP response returned with status code: " + response.statusCode());
        }
    }

    public void deleteUserMapping(UUID systemId, String externalUserId) {
        var request = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId)
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete user mapping, HTTP response returned with status code: " + response.statusCode());
        }
    }

    // --- User Mapping Attributes ---

    public List<Attribute> getUserMappingAttributes(UUID systemId, String externalUserId, List<String> keys) {
        var requestBuilder = this.requestBuilder(GET,
                SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId + "/attributes");
        if (keys != null) {
            keys.forEach(key -> requestBuilder.queryParameter("key", key));
        }
        return sendThenMapAs(requestBuilder.build(), Attribute.ListOf.class);
    }

    public void createUserMappingAttributes(UUID systemId, String externalUserId, List<AttributeInput> attributes) {
        var request = this.requestBuilder(POST,
                SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to create user mapping attributes, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void replaceUserMappingAttributes(UUID systemId, String externalUserId, List<AttributeInput> attributes) {
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to replace user mapping attributes, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void replaceUserMappingAttributeValues(UUID systemId, String externalUserId, String key,
            List<String> values) {
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId + "/attributes/" + key)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(values))
                          .build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to replace user mapping attribute values, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    public void deleteUserMappingAttribute(UUID systemId, String externalUserId, String key) {
        var request = this.requestBuilder(DELETE,
                SYSTEMS_PATH + "/" + systemId + "/user-mappings/" + externalUserId + "/attributes/" + key).build();
        var response = sendThenReadAsString(request);
        if (response.statusCode() != 204) {
            ErrorUtils.throwException(response,
                    "Failed to delete user mapping attribute, HTTP response returned with status code: "
                            + response.statusCode());
        }
    }

    // --- Principal Users ---

    public PrincipalUserMapping.PaginatedListOf getPrincipalUserMappings(UUID principalUserId, String cursor,
            Integer limit) {
        var requestBuilder = this.requestBuilder(GET, PRINCIPAL_USERS_PATH + "/" + principalUserId + "/user-mappings");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), PrincipalUserMapping.PaginatedListOf.class);
    }

    public PrincipalUserMembership.PaginatedListOf getPrincipalUserMembership(UUID principalUserId, String cursor,
            Integer limit) {
        var requestBuilder = this.requestBuilder(GET, PRINCIPAL_USERS_PATH + "/" + principalUserId + "/membership");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), PrincipalUserMembership.PaginatedListOf.class);
    }

    // --- Users ---

    public InteractiveUser.PaginatedListOf listUsers(String externalId, String cursor, Integer limit) {
        var requestBuilder = this.requestBuilder(GET, USERS_PATH);
        if (externalId != null) {
            requestBuilder.queryParameter("externalid", externalId);
        }
        if (cursor != null) {
            requestBuilder.queryParameter("cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), InteractiveUser.PaginatedListOf.class);
    }

    public InteractiveUser getUser(UUID userId) {
        var request = this.requestBuilder(GET, USERS_PATH + "/" + userId).build();
        return sendThenMapAs(request, InteractiveUser.class);
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, NucleusHttpClient> {

        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        public Builder hxpEnvironment(String environment) {
            return header("hxp-environment", environment);
        }

        @Override
        public NucleusHttpClient build() {
            return new NucleusHttpClient(this);
        }
    }
}
