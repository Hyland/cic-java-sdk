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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.governance.object.ContentEvent;
import org.hyland.sdk.cic.governance.object.ContentEventData;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;

/**
 * @since 1.2.0
 */
public class GovernanceServiceTest {

    private TestGovernanceHttpClient httpClient;

    private GovernanceService service;

    @BeforeEach
    public void setUp() {
        httpClient = new TestGovernanceHttpClient();
        service = new GovernanceService(httpClient);
    }

    @Test
    public void testSendEvent() {
        var data = ContentEventData.builder("content-123", "env-1", "document.pdf", "primary").build();
        var event = ContentEvent.builder(ContentEvent.Type.CREATED, ContentEvent.DataSourceType.NUXEO, data)
                                .time(Instant.ofEpochMilli(1609459200000L))
                                .build();

        service.sendEvent(event);

        assertEquals(1, httpClient.sendEventCalls.size());
        assertEquals(event, httpClient.sendEventCalls.get(0));
    }

    private static class TestGovernanceHttpClient extends GovernanceHttpClient {

        List<ContentEvent> sendEventCalls = new ArrayList<>();

        TestGovernanceHttpClient() {
            super(GovernanceHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public void sendEvent(ContentEvent event) {
            sendEventCalls.add(event);
        }
    }
}
