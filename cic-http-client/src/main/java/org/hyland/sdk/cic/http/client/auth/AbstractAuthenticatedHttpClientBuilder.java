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

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient.Builder;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClientBuilder;

/**
 * @since 1.0.0
 */
public abstract class AbstractAuthenticatedHttpClientBuilder<B extends AbstractAuthenticatedHttpClientBuilder<B, C>, C extends AbstractAuthenticatedHttpClient>
        extends AbstractHttpClientBuilder<B, C> {

    protected final Builder authenticationBuilder;

    protected AbstractAuthenticatedHttpClientBuilder(String baseUrl,
            AuthenticationHttpClient.Builder authenticationBuilder) {
        super(baseUrl);
        this.authenticationBuilder = authenticationBuilder;
    }

    public B clientId(String clientId) {
        authenticationBuilder.clientId(clientId);
        return self();
    }

    public B clientSecret(String clientSecret) {
        authenticationBuilder.clientSecret(clientSecret);
        return self();
    }

    public B grantType(String grantType) {
        authenticationBuilder.grantType(grantType);
        return self();
    }

    public B scope(String scope) {
        authenticationBuilder.scope(scope);
        return self();
    }
}
