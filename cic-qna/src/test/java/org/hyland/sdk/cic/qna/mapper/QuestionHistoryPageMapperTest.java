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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;

/**
 * @since 1.0.0
 */
class QuestionHistoryPageMapperTest {

    @Test
    void testDeserializeWithData() {
        var json = """
                {
                  "data": [
                    {
                      "id": "q1",
                      "question": "What is the status?",
                      "answer": "Report is complete.",
                      "dateCreated": "2026-04-01T10:00:00Z",
                      "dateAnswered": "2026-04-01T10:00:05Z",
                      "agentVersion": 2,
                      "responseCompleteness": "Complete",
                      "feedback": "Good",
                      "staticFilter": null,
                      "dynamicFilter": null
                    },
                    {
                      "id": "q2",
                      "question": "Any updates?",
                      "answer": null,
                      "dateCreated": "2026-04-02T08:00:00Z",
                      "dateAnswered": null,
                      "agentVersion": 1,
                      "responseCompleteness": "Error",
                      "feedback": null,
                      "staticFilter": null,
                      "dynamicFilter": null
                    }
                  ],
                  "pagination": {
                    "pageSize": 10,
                    "pageNumber": 1,
                    "totalItems": 2,
                    "totalPages": 1
                  }
                }
                """;

        var page = MapperService.read(json, QuestionHistoryPage.class);

        assertEquals(2, page.data().size());

        var first = page.data().get(0);
        assertEquals("q1", first.id());
        assertEquals("What is the status?", first.question());
        assertEquals("Report is complete.", first.answer());
        assertEquals(ResponseCompleteness.COMPLETE, first.responseCompleteness());
        assertEquals(FeedbackType.GOOD, first.feedback());
        assertEquals(2, first.agentVersion());
        assertNull(first.staticFilter());

        var second = page.data().get(1);
        assertEquals("q2", second.id());
        assertNull(second.answer());
        assertEquals(ResponseCompleteness.ERROR, second.responseCompleteness());
        assertNull(second.feedback());

        assertNotNull(page.pagination());
        assertEquals(10, page.pagination().pageSize());
        assertEquals(1, page.pagination().pageNumber());
        assertEquals(2, page.pagination().totalItems());
        assertEquals(1, page.pagination().totalPages());
    }

    @Test
    void testDeserializeEmptyPage() {
        var json = """
                {
                  "data": [],
                  "pagination": {
                    "pageSize": 100,
                    "pageNumber": 1,
                    "totalItems": 0,
                    "totalPages": 0
                  }
                }
                """;

        var page = MapperService.read(json, QuestionHistoryPage.class);

        assertEquals(0, page.data().size());
        assertEquals(0, page.pagination().totalItems());
    }
}
