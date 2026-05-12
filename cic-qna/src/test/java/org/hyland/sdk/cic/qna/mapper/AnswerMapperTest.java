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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;

/**
 * @since 1.0.0
 */
class AnswerMapperTest {

    @Test
    void testDeserializeFullAnswer() {
        var json = """
                {
                  "answer": "The current financial report is complete and approved.",
                  "agentId": "f755cbcd-40a6-46b7-8f09-4b6ebe02c5fe",
                  "agentVersion": 2,
                  "responseCompleteness": "Complete",
                  "objectReferences": [
                    {
                      "objectId": "doc123",
                      "references": [
                        {"referenceId": "ref123", "rankScore": 0.58, "rank": 1},
                        {"referenceId": "ref456", "rankScore": 0.45, "rank": 2}
                      ]
                    }
                  ],
                  "graphDocumentReferences": [],
                  "question": "What is the status of the current financial report?",
                  "feedback": "Good",
                  "staticFilter": null,
                  "dynamicFilter": null,
                  "hxqlFilter": null
                }
                """;

        var answer = MapperService.read(json, Answer.class);

        assertEquals("The current financial report is complete and approved.", answer.answer());
        assertEquals("f755cbcd-40a6-46b7-8f09-4b6ebe02c5fe", answer.agentId());
        assertEquals(2, answer.agentVersion());
        assertEquals(ResponseCompleteness.COMPLETE, answer.responseCompleteness());
        assertNotNull(answer.objectReferences());
        assertEquals(1, answer.objectReferences().size());
        assertEquals("doc123", answer.objectReferences().get(0).objectId());
        assertEquals(2, answer.objectReferences().get(0).references().size());
        assertEquals("ref123", answer.objectReferences().get(0).references().get(0).referenceId());
        assertEquals(0.58, answer.objectReferences().get(0).references().get(0).rankScore(), 0.001);
        assertEquals(1, answer.objectReferences().get(0).references().get(0).rank());
        assertNotNull(answer.graphDocumentReferences());
        assertTrue(answer.graphDocumentReferences().isEmpty());
        assertEquals("What is the status of the current financial report?", answer.question());
        assertEquals(FeedbackType.GOOD, answer.feedback());
        assertNull(answer.staticFilter());
        assertNull(answer.dynamicFilter());
        assertNull(answer.hxqlFilter());
    }

    @Test
    void testDeserializeAnswerWithGraphDocumentReferences() {
        var json = """
                {
                  "answer": "Paris is the capital of France.",
                  "agentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "agentVersion": 1,
                  "responseCompleteness": "Complete",
                  "objectReferences": [],
                  "graphDocumentReferences": [
                    {
                      "documentId": "gdoc-abc",
                      "references": [
                        {"referenceId": "gref-1", "rankScore": 0.92, "rank": 1}
                      ]
                    },
                    {
                      "documentId": "gdoc-xyz",
                      "references": [
                        {"referenceId": "gref-2", "rankScore": 0.75, "rank": 2},
                        {"referenceId": "gref-3", "rankScore": 0.61, "rank": 3}
                      ]
                    }
                  ],
                  "question": "What is the capital of France?",
                  "feedback": null,
                  "staticFilter": null,
                  "dynamicFilter": null,
                  "hxqlFilter": null
                }
                """;

        var answer = MapperService.read(json, Answer.class);

        assertNotNull(answer.graphDocumentReferences());
        assertEquals(2, answer.graphDocumentReferences().size());
        assertEquals("gdoc-abc", answer.graphDocumentReferences().get(0).documentId());
        assertEquals(1, answer.graphDocumentReferences().get(0).references().size());
        assertEquals("gref-1", answer.graphDocumentReferences().get(0).references().get(0).referenceId());
        assertEquals(0.92, answer.graphDocumentReferences().get(0).references().get(0).rankScore(), 0.001);
        assertEquals(1, answer.graphDocumentReferences().get(0).references().get(0).rank());
        assertEquals("gdoc-xyz", answer.graphDocumentReferences().get(1).documentId());
        assertEquals(2, answer.graphDocumentReferences().get(1).references().size());
        assertEquals("gref-2", answer.graphDocumentReferences().get(1).references().get(0).referenceId());
        assertEquals(0.75, answer.graphDocumentReferences().get(1).references().get(0).rankScore(), 0.001);
        assertEquals(2, answer.graphDocumentReferences().get(1).references().get(0).rank());
        assertTrue(answer.objectReferences().isEmpty());
    }

    @Test
    void testDeserializeMinimalAnswer() {
        var json = """
                {
                  "answer": null,
                  "agentId": "f755cbcd-40a6-46b7-8f09-4b6ebe02c5fe",
                  "agentVersion": 1,
                  "responseCompleteness": "Error",
                  "objectReferences": null,
                  "question": null,
                  "feedback": null
                }
                """;

        var answer = MapperService.read(json, Answer.class);

        assertNull(answer.answer());
        assertEquals(ResponseCompleteness.ERROR, answer.responseCompleteness());
        assertTrue(answer.objectReferences().isEmpty());
        assertTrue(answer.graphDocumentReferences().isEmpty());
        assertNull(answer.feedback());
    }
}
