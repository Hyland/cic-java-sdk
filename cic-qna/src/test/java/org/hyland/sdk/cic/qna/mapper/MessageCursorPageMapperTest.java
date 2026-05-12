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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.MessagePage;
import org.hyland.sdk.cic.qna.object.MessageStatus;

/**
 * @since 1.0.0
 */
class MessageCursorPageMapperTest {

    @Test
    void testDeserializeWithData() {
        var json = """
                {
                  "data": [
                    {
                      "id": "11111111-2222-3333-4444-555555555555",
                      "question": "What is the status?",
                      "answer": "The report is done.",
                      "documentReferences": [],
                      "feedback": null,
                      "dateCreated": "2026-04-02T11:42:00Z",
                      "dateAnswered": "2026-04-02T11:42:01Z",
                      "agentVersion": 2,
                      "status": "Answered"
                    }
                  ],
                  "pagination": {
                    "nextCursor": "msg-cursor-123",
                    "hasMore": false
                  }
                }
                """;

        var page = MapperService.read(json, MessagePage.class);

        assertEquals(1, page.data().size());
        assertEquals("11111111-2222-3333-4444-555555555555", page.data().get(0).id());
        assertEquals("What is the status?", page.data().get(0).question());
        assertEquals("The report is done.", page.data().get(0).answer());
        assertEquals(MessageStatus.ANSWERED, page.data().get(0).status());
        assertEquals("msg-cursor-123", page.pagination().nextCursor());
        assertFalse(page.pagination().hasMore());
    }

    @Test
    void testDeserializeEmptyPage() {
        var json = """
                {
                  "data": [],
                  "pagination": {
                    "nextCursor": null,
                    "hasMore": false
                  }
                }
                """;

        var page = MapperService.read(json, MessagePage.class);

        assertEquals(0, page.data().size());
        assertNull(page.pagination().nextCursor());
        assertFalse(page.pagination().hasMore());
    }
}
