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
import org.hyland.sdk.cic.qna.object.ConversationMessage;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.MessageStatus;

/**
 * @since 1.0.0
 */
class ConversationMessageMapperTest {

    @Test
    void testDeserializeWithDocumentReferences() {
        var json = """
                {
                  "id": "11111111-2222-3333-4444-555555555555",
                  "question": "What is the status?",
                  "answer": "The report is done.",
                  "documentReferences": [
                    {
                      "documentId": "doc1",
                      "references": [
                        {"referenceId": "ref1", "rankScore": 0.9, "rank": 1}
                      ]
                    }
                  ],
                  "graphDocumentReferences": [],
                  "feedback": "Good",
                  "staticFilter": null,
                  "dynamicFilter": null,
                  "dateCreated": "2026-04-02T11:42:00Z",
                  "dateAnswered": "2026-04-02T11:42:01Z",
                  "agentVersion": 2,
                  "status": "Answered"
                }
                """;

        var message = MapperService.read(json, ConversationMessage.class);

        assertEquals("11111111-2222-3333-4444-555555555555", message.id());
        assertEquals("What is the status?", message.question());
        assertEquals("The report is done.", message.answer());
        assertEquals(1, message.documentReferences().size());
        assertEquals("doc1", message.documentReferences().get(0).documentId());
        assertEquals(1, message.documentReferences().get(0).references().size());
        assertEquals("ref1", message.documentReferences().get(0).references().get(0).referenceId());
        assertNotNull(message.graphDocumentReferences());
        assertTrue(message.graphDocumentReferences().isEmpty());
        assertEquals(FeedbackType.GOOD, message.feedback());
        assertNull(message.staticFilter());
        assertEquals(MessageStatus.ANSWERED, message.status());
        assertEquals(2, message.agentVersion());
    }

    @Test
    void testDeserializeWithGraphDocumentReferences() {
        var json = """
                {
                  "id": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
                  "question": "What is an orbit?",
                  "answer": "An orbit is a curved path.",
                  "documentReferences": [],
                  "graphDocumentReferences": [
                    {
                      "documentId": "gdoc-1",
                      "references": [
                        {"referenceId": "gref-1", "rankScore": 0.88, "rank": 1},
                        {"referenceId": "gref-2", "rankScore": 0.72, "rank": 2}
                      ]
                    }
                  ],
                  "feedback": null,
                  "staticFilter": {
                    "field": "purpose",
                    "type": "Text",
                    "operator": "Any",
                    "value": "",
                    "values": ["log-file-searcher"]
                  },
                  "dynamicFilter": {
                    "field": "DateCreated",
                    "type": "Date",
                    "operator": "Equals",
                    "value": "2024-09-08"
                  },
                  "dateCreated": "2026-04-08T14:52:59Z",
                  "dateAnswered": "2026-04-08T14:52:59Z",
                  "agentVersion": 2,
                  "status": "Answered"
                }
                """;

        var message = MapperService.read(json, ConversationMessage.class);

        assertTrue(message.documentReferences().isEmpty());
        assertNotNull(message.graphDocumentReferences());
        assertEquals(1, message.graphDocumentReferences().size());
        assertEquals("gdoc-1", message.graphDocumentReferences().get(0).documentId());
        assertEquals(2, message.graphDocumentReferences().get(0).references().size());
        assertEquals("gref-1", message.graphDocumentReferences().get(0).references().get(0).referenceId());
        assertEquals(0.88, message.graphDocumentReferences().get(0).references().get(0).rankScore(), 0.001);
        assertEquals(1, message.graphDocumentReferences().get(0).references().get(0).rank());
        assertNotNull(message.staticFilter());
        assertNotNull(message.dynamicFilter());
        assertEquals(MessageStatus.ANSWERED, message.status());
    }

    @Test
    void testDeserializeMinimal() {
        var json = """
                {
                  "id": "11111111-2222-3333-4444-555555555555",
                  "question": "Hello?",
                  "answer": null,
                  "documentReferences": null,
                  "feedback": null,
                  "dateCreated": "2026-04-02T11:42:00Z",
                  "dateAnswered": null,
                  "agentVersion": 1,
                  "status": "Submitted"
                }
                """;

        var message = MapperService.read(json, ConversationMessage.class);

        assertEquals("Hello?", message.question());
        assertNull(message.answer());
        assertNotNull(message.documentReferences());
        assertTrue(message.documentReferences().isEmpty());
        assertNotNull(message.graphDocumentReferences());
        assertTrue(message.graphDocumentReferences().isEmpty());
        assertNull(message.feedback());
        assertEquals(MessageStatus.SUBMITTED, message.status());
    }

    @Test
    void testDeserializeWithDocumentReferencesWithDifferentCase() {
        var json = """
                {
                  "id": "11111111-2222-3333-4444-555555555555",
                  "question": "What is the status?",
                  "answer": "The report is done.",
                  "documentReferences": [
                    {
                      "documentId": "doc1",
                      "references": [
                        {"referenceId": "ref1", "rankScore": 0.9, "rank": 1}
                      ]
                    }
                  ],
                  "graphDocumentReferences": [],
                  "feedback": "Good",
                  "staticFilter": null,
                  "dynamicFilter": null,
                  "dateCreated": "2026-04-02T11:42:00Z",
                  "dateAnswered": "2026-04-02T11:42:01Z",
                  "agentVersion": 2,
                  "status": "answered"
                }
                """;

        var message = MapperService.read(json, ConversationMessage.class);

        assertEquals("11111111-2222-3333-4444-555555555555", message.id());
        assertEquals("What is the status?", message.question());
        assertEquals("The report is done.", message.answer());
        assertEquals(1, message.documentReferences().size());
        assertEquals("doc1", message.documentReferences().get(0).documentId());
        assertEquals(1, message.documentReferences().get(0).references().size());
        assertEquals("ref1", message.documentReferences().get(0).references().get(0).referenceId());
        assertNotNull(message.graphDocumentReferences());
        assertTrue(message.graphDocumentReferences().isEmpty());
        assertEquals(FeedbackType.GOOD, message.feedback());
        assertNull(message.staticFilter());
        assertEquals(MessageStatus.ANSWERED, message.status());
        assertEquals(2, message.agentVersion());
    }
}
