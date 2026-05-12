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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.ConversationPage;

/**
 * @since 1.0.0
 */
class ConversationCursorPageMapperTest {

    @Test
    void testDeserializeWithData() {
        var json = """
                {
                  "data": [
                    {
                      "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                      "name": "My Conversation",
                      "description": "About reports",
                      "lastModified": "2026-04-02T11:42:00Z"
                    },
                    {
                      "id": "11111111-2222-3333-4444-555555555555",
                      "name": "Another Conv",
                      "description": null,
                      "lastModified": "2026-04-01T10:00:00Z"
                    }
                  ],
                  "pagination": {
                    "nextCursor": "cursor-abc",
                    "hasMore": true
                  }
                }
                """;

        var page = MapperService.read(json, ConversationPage.class);

        assertEquals(2, page.data().size());
        assertEquals("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", page.data().get(0).id());
        assertEquals("My Conversation", page.data().get(0).name());
        assertEquals("About reports", page.data().get(0).description());
        assertEquals("11111111-2222-3333-4444-555555555555", page.data().get(1).id());
        assertEquals("cursor-abc", page.pagination().nextCursor());
        assertTrue(page.pagination().hasMore());
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

        var page = MapperService.read(json, ConversationPage.class);

        assertEquals(0, page.data().size());
        assertNull(page.pagination().nextCursor());
        assertFalse(page.pagination().hasMore());
    }
}
