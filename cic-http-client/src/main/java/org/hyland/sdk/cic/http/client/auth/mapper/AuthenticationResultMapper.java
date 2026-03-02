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
package org.hyland.sdk.cic.http.client.auth.mapper;

import java.time.Instant;

import org.hyland.sdk.cic.http.client.auth.object.AuthenticationResult;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class AuthenticationResultMapper implements CICMapper<AuthenticationResult> {

    @Override
    public AuthenticationResult fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        String accessToken = cicObject.getStringOrThrow("access_token");
        String tokenType = cicObject.getStringOrThrow("token_type");
        int expiresIn = cicObject.getIntOrThrow("expires_in");
        Instant expiresAt = Instant.now().plusSeconds(expiresIn);
        String scope = cicObject.getStringOrThrow("scope");
        return new AuthenticationResult(accessToken, tokenType, expiresAt, scope);
    }
}
