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
import java.util.Objects;

import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;
import org.hyland.sdk.cic.nucleus.object.GroupCreateInput;
import org.hyland.sdk.cic.nucleus.object.GroupMember;
import org.hyland.sdk.cic.nucleus.object.GroupMemberAssignmentInput;
import org.hyland.sdk.cic.nucleus.object.GroupOutput;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMapping;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.SystemOutput;
import org.hyland.sdk.cic.nucleus.object.UserMapping;
import org.hyland.sdk.cic.nucleus.object.UserMappingCreateInput;
import org.hyland.sdk.cic.nucleus.object.UserMappingReplaceInput;

/**
 * HTTP client for interacting with the Nucleus System Integrations API.
 *
 * @since 1.0.0
 */
public class NucleusHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String SYSTEMS_PATH = "/system-integrations/systems";

    private static final String PRINCIPAL_USERS_PATH = "/system-integrations/principal-users";

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

    public SystemOutput getSystem(String systemId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        var request = this.requestBuilder(GET, SYSTEMS_PATH + "/" + encodePathSegment(systemId)).build();
        return sendThenMapAs(request, SystemOutput.class);
    }

    public GroupOutput.PaginatedListOf listGroups(String systemId, String cursor, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), GroupOutput.PaginatedListOf.class);
    }

    public GroupOutput getGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        var request = this.requestBuilder(GET,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/" + encodePathSegment(externalGroupId))
                          .build();
        return sendThenMapAs(request, GroupOutput.class);
    }

    public void createGroups(String systemId, List<GroupCreateInput> groups) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(groups, "groups cannot be null");
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(groups))
                          .build();
        sendExpectingSuccess(request, "Failed to create groups");
    }

    public void deleteGroup(String systemId, String externalGroupId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        var request = this.requestBuilder(DELETE,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/" + encodePathSegment(externalGroupId))
                          .build();
        sendExpectingSuccess(request, "Failed to delete group");
    }

    public List<Attribute> getGroupAttributes(String systemId, String externalGroupId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/"
                + encodePathSegment(externalGroupId) + "/attributes");
        if (keys != null) {
            keys.forEach(key -> requestBuilder.queryParameter("key", key));
        }
        return sendThenMapAs(requestBuilder.build(), Attribute.ListOf.class);
    }

    public void createGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        var request = this.requestBuilder(POST,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/" + encodePathSegment(externalGroupId)
                        + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        sendExpectingSuccess(request, "Failed to create group attributes");
    }

    public void replaceGroupAttributes(String systemId, String externalGroupId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/" + encodePathSegment(externalGroupId)
                        + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        sendExpectingSuccess(request, "Failed to replace group attributes");
    }

    public void replaceGroupAttributeValues(String systemId, String externalGroupId, String key, List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/" + encodePathSegment(externalGroupId)
                        + "/attributes/" + encodePathSegment(key))
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(values))
                          .build();
        sendExpectingSuccess(request, "Failed to replace group attribute values");
    }

    public void deleteGroupAttribute(String systemId, String externalGroupId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        var request = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/groups/"
                + encodePathSegment(externalGroupId) + "/attributes/" + encodePathSegment(key)).build();
        sendExpectingSuccess(request, "Failed to delete group attribute");
    }

    public GroupMember.PaginatedListOf listGroupMembers(String systemId, String externalGroupId, String cursor,
            Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        var requestBuilder = this.requestBuilder(GET,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/group-members");
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

    public void assignGroupMembers(String systemId, List<GroupMemberAssignmentInput> assignments) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(assignments, "assignments cannot be null");
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/group-members")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(assignments))
                          .build();
        sendExpectingSuccess(request, "Failed to assign group members");
    }

    public void removeGroupMembers(String systemId, String parentExternalGroupId, List<String> memberExternalUserIds,
            List<String> memberExternalGroupIds) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        var requestBuilder = this.requestBuilder(DELETE,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/group-members");
        if (parentExternalGroupId != null) {
            requestBuilder.queryParameter("parentExternalGroupId", parentExternalGroupId);
        }
        if (memberExternalUserIds != null) {
            memberExternalUserIds.forEach(id -> requestBuilder.queryParameter("memberExternalUserIds", id));
        }
        if (memberExternalGroupIds != null) {
            memberExternalGroupIds.forEach(id -> requestBuilder.queryParameter("memberExternalGroupIds", id));
        }
        sendExpectingSuccess(requestBuilder.build(), "Failed to remove group members");
    }

    public UserMapping.PaginatedListOf listUserMappings(String systemId, String cursor, Integer limit) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        var requestBuilder = this.requestBuilder(GET,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), UserMapping.PaginatedListOf.class);
    }

    public UserMapping getUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        var request = this.requestBuilder(GET, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/"
                + encodePathSegment(externalUserId)).build();
        return sendThenMapAs(request, UserMapping.class);
    }

    public void createUserMappings(String systemId, List<UserMappingCreateInput> mappings) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(mappings, "mappings cannot be null");
        var request = this.requestBuilder(POST, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(mappings))
                          .build();
        sendExpectingSuccess(request, "Failed to create user mappings");
    }

    public void updateUserMapping(String systemId, String externalUserId, UserMappingReplaceInput input) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(input, "input cannot be null");
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/"
                        + encodePathSegment(externalUserId))
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(input))
                          .build();
        sendExpectingSuccess(request, "Failed to update user mapping");
    }

    public void deleteUserMapping(String systemId, String externalUserId) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        var request = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/"
                + encodePathSegment(externalUserId)).build();
        sendExpectingSuccess(request, "Failed to delete user mapping");
    }

    // --- User Mapping Attributes ---

    public List<Attribute> getUserMappingAttributes(String systemId, String externalUserId, List<String> keys) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        var requestBuilder = this.requestBuilder(GET, SYSTEMS_PATH + "/" + encodePathSegment(systemId)
                + "/user-mappings/" + encodePathSegment(externalUserId) + "/attributes");
        if (keys != null) {
            keys.forEach(key -> requestBuilder.queryParameter("key", key));
        }
        return sendThenMapAs(requestBuilder.build(), Attribute.ListOf.class);
    }

    public void createUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        var request = this.requestBuilder(POST,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/" + encodePathSegment(externalUserId)
                        + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        sendExpectingSuccess(request, "Failed to create user mapping attributes");
    }

    public void replaceUserMappingAttributes(String systemId, String externalUserId, List<AttributeInput> attributes) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/" + encodePathSegment(externalUserId)
                        + "/attributes")
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(attributes))
                          .build();
        sendExpectingSuccess(request, "Failed to replace user mapping attributes");
    }

    public void replaceUserMappingAttributeValues(String systemId, String externalUserId, String key,
            List<String> values) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(values, "values cannot be null");
        var request = this.requestBuilder(PUT,
                SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/" + encodePathSegment(externalUserId)
                        + "/attributes/" + encodePathSegment(key))
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(values))
                          .build();
        sendExpectingSuccess(request, "Failed to replace user mapping attribute values");
    }

    public void deleteUserMappingAttribute(String systemId, String externalUserId, String key) {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        Objects.requireNonNull(key, "key cannot be null");
        var request = this.requestBuilder(DELETE, SYSTEMS_PATH + "/" + encodePathSegment(systemId) + "/user-mappings/"
                + encodePathSegment(externalUserId) + "/attributes/" + encodePathSegment(key)).build();
        sendExpectingSuccess(request, "Failed to delete user mapping attribute");
    }

    public PrincipalUserMapping.PaginatedListOf listPrincipalUserMappings(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        var requestBuilder = this.requestBuilder(GET,
                PRINCIPAL_USERS_PATH + "/" + encodePathSegment(principalUserId) + "/user-mappings");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), PrincipalUserMapping.PaginatedListOf.class);
    }

    public PrincipalUserMembership.PaginatedListOf listPrincipalUserMemberships(String principalUserId, String cursor,
            Integer limit) {
        Objects.requireNonNull(principalUserId, "principalUserId cannot be null");
        var requestBuilder = this.requestBuilder(GET,
                PRINCIPAL_USERS_PATH + "/" + encodePathSegment(principalUserId) + "/membership");
        if (cursor != null) {
            requestBuilder.queryParameter("Cursor", cursor);
        }
        if (limit != null) {
            requestBuilder.queryParameter("Limit", limit.toString());
        }
        return sendThenMapAs(requestBuilder.build(), PrincipalUserMembership.PaginatedListOf.class);
    }

    /**
     * Sends a request that is expected to complete with a success (2xx) status code and has no response body to map.
     *
     * @param request the request to send
     * @param failureMessage the message used for the thrown exception if the service responds with an error status
     * @throws org.hyland.sdk.cic.http.client.CICServiceException if the service responds with a 4xx or 5xx status code
     */
    private void sendExpectingSuccess(CICHttpRequest request, String failureMessage) {
        var response = sendThenReadAsString(request);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response, failureMessage);
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, NucleusHttpClient> {

        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        @Override
        public NucleusHttpClient build() {
            return new NucleusHttpClient(this);
        }
    }
}
