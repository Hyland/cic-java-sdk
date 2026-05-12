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
package org.hyland.sdk.cic.qna.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.MessageStatus;
import org.hyland.sdk.cic.qna.object.StartConversationResponse;

/**
 * @since 1.0.0
 */
class StartConversationResponseMapperTest {

    @Test
    void testDeserialize() {
        var json = """
                {
                  "conversation": {
                    "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                    "name": "My Conversation",
                    "description": "About reports",
                    "lastModified": "2026-04-02T11:42:00Z"
                  },
                  "message": {
                    "id": "11111111-2222-3333-4444-555555555555",
                    "question": "What is the status?",
                    "answer": "The report is done.",
                    "documentReferences": [],
                    "graphDocumentReferences": [],
                    "feedback": null,
                    "dateCreated": "2026-04-02T11:42:00Z",
                    "dateAnswered": "2026-04-02T11:42:01Z",
                    "agentVersion": 2,
                    "status": "Answered"
                  }
                }
                """;

        var response = MapperService.read(json, StartConversationResponse.class);

        assertNotNull(response.conversation());
        assertEquals("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", response.conversation().id());
        assertEquals("My Conversation", response.conversation().name());
        assertNotNull(response.message());
        assertEquals("11111111-2222-3333-4444-555555555555", response.message().id());
        assertEquals("What is the status?", response.message().question());
        assertEquals("The report is done.", response.message().answer());
        assertTrue(response.message().documentReferences().isEmpty());
        assertTrue(response.message().graphDocumentReferences().isEmpty());
        assertEquals(MessageStatus.ANSWERED, response.message().status());
        assertEquals(2, response.message().agentVersion());
    }
}
