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

import java.time.Instant;
import java.util.function.Supplier;

import org.hyland.sdk.cic.http.client.auth.object.AuthenticationResult;
import org.hyland.sdk.cic.http.client.base.AbstractHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest;

/**
 * @since 1.0.0
 */
public abstract class AbstractAuthenticatedHttpClient extends AbstractHttpClient {

    protected final AuthenticationHttpClient authenticationClient;

    protected volatile AuthenticationResult authenticationResult;

    protected AbstractAuthenticatedHttpClient(AbstractAuthenticatedHttpClientBuilder<?, ?> builder) {
        super(builder);
        this.authenticationClient = builder.authenticationBuilder.build();
    }

    @Override
    protected void appendHeaders(CICHttpRequest.Builder requestBuilder) {
        super.appendHeaders(requestBuilder);
        requestBuilder.header("Authorization", "Bearer " + retrieveToken());
    }

    protected String retrieveToken() {
        Supplier<Boolean> renewToken = () -> authenticationResult == null //
                || authenticationResult.expiresAt().isBefore(Instant.now());
        if (renewToken.get()) {
            synchronized (this) {
                if (renewToken.get()) {
                    authenticationResult = authenticationClient.getToken();
                }
            }
        }
        return authenticationResult.accessToken();
    }
}
