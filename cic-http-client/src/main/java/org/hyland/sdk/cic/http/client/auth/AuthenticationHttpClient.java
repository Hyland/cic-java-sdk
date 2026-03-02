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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.http.client.auth;

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.object.AuthenticationResult;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClient;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClientBuilder;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest;

/**
 * @since 1.0.0
 */
public class AuthenticationHttpClient extends AbstractHttpClient {

    protected final String clientId;

    protected final String clientSecret;

    protected final String grantType;

    protected final Set<String> scopes;

    // -------------
    // Instantiation
    // -------------

    protected AuthenticationHttpClient(Builder builder) {
        super(builder);
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.grantType = builder.grantType;
        this.scopes = Collections.unmodifiableSet(new LinkedHashSet<>(builder.scopes));
    }

    public static Builder from() {
        // TODO turn this to production
        return from("https://auth.iam.dev.experience.hyland.com");
    }

    public static Builder from(String baseUrl) {
        return new Builder(baseUrl);
    }

    // ------------
    // Service APIs
    // ------------

    protected AuthenticationResult getToken() {
        var request = requestBuilder(POST).header("Content-Type", "application/x-www-form-urlencoded")
                                          .entity(CICHttpRequest.FormDataEntity.ofMap(
                                                  Map.of("client_id", clientId, "client_secret", clientSecret,
                                                          "grant_type", grantType, "scope", String.join(" ", scopes))))
                                          .build();
        return sendThenMapAs(request, AuthenticationResult.class);
    }

    public static class Builder extends AbstractHttpClientBuilder<Builder, AuthenticationHttpClient> {

        protected final Set<String> scopes = new LinkedHashSet<>();

        protected String clientId;

        protected String clientSecret;

        protected String grantType = "client_credentials";

        public Builder(String baseUrl) {
            super(baseUrl);
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder grantType(String grantType) {
            this.grantType = grantType;
            return this;
        }

        public Builder scope(String scope) {
            this.scopes.add(scope);
            return this;
        }

        /**
         * @return a {@link AuthenticationHttpClient} suitable for other clients usage
         * @throws CICSdkException if required configuration is missing
         */
        public AuthenticationHttpClient build() {
            if (clientId == null || clientSecret == null) {
                throw new CICSdkException("clientId and clientSecret must be provided");
            }
            return new AuthenticationHttpClient(this);
        }
    }
}
