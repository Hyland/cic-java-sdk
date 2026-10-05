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
package org.hyland.sdk.cic.governance;

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;

import org.hyland.sdk.cic.governance.object.ContentEvent;
import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;

/**
 * HTTP client for interacting with the CIC Governance service.
 *
 * @since 1.2.0
 */
public class GovernanceHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String RECORDS_EVENT_PATH = "/api/records/event";

    // ---------------
    // Instantiation
    // ---------------

    protected GovernanceHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from() {
        // TODO turn this to production
        return from("https://api.governance.sandbox.experience.hyland.com");
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    // --------------
    // Service APIs
    // --------------

    /**
     * Sends a content event to the CIC Governance service.
     *
     * @param event the content event to send
     * @throws CICSdkException if the request fails or returns a non-202 status code
     */
    public void sendEvent(ContentEvent event) {
        var request = this.requestBuilder(POST, RECORDS_EVENT_PATH)
                          .header("Content-Type", "application/json")
                          .entity(new CICEntity(event))
                          .build();

        var response = sendThenReadAsString(request);
        int statusCode = response.statusCode();
        if (statusCode != 202) {
            ErrorUtils.throwException(response,
                    "Couldn't send the content event, HTTP response returned with status code: " + statusCode);
        }
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, GovernanceHttpClient> {

        /**
         * Creates a builder for GovernanceHttpClient.
         *
         * @param baseUrl the base URL of the CIC Governance service
         * @param authenticationBuilder the authentication builder
         */
        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        @Override
        public GovernanceHttpClient build() {
            return new GovernanceHttpClient(this);
        }
    }

}
