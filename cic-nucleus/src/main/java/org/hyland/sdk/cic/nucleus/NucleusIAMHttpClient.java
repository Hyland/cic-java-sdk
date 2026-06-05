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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;

import java.util.Objects;

import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;

/**
 * HTTP client for interacting with the Nucleus IAM API.
 *
 * @since 1.0.0
 */
public class NucleusIAMHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String USERS_PATH = "/api/users";

    protected NucleusIAMHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

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

    public InteractiveUser getUser(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        var request = this.requestBuilder(GET, USERS_PATH + "/" + encodePathSegment(userId)).build();
        return sendThenMapAs(request, InteractiveUser.class);
    }

    public static class Builder
            extends AbstractAuthenticatedHttpClientBuilder<NucleusIAMHttpClient.Builder, NucleusIAMHttpClient> {

        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        @Override
        public NucleusIAMHttpClient build() {
            return new NucleusIAMHttpClient(this);
        }
    }
}
