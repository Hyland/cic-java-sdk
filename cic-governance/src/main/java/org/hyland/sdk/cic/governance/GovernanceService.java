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

import org.hyland.sdk.cic.governance.object.ContentEvent;
import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * High-level service for CIC Governance API operations.
 *
 * @since 1.2.0
 */
public class GovernanceService {

    protected final GovernanceHttpClient httpClient;

    public GovernanceService(GovernanceHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends a content event to the CIC Governance service.
     *
     * @param event the content event to send
     * @throws CICSdkException if the request fails
     */
    public void sendEvent(ContentEvent event) {
        httpClient.sendEvent(event);
    }
}
