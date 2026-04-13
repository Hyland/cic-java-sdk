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
package org.hyland.sdk.cic.qna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;

/**
 * @since 1.0.0
 */
class IntegrationQnaServiceTest {

    private TestQnaHttpClient httpClient;

    private IntegrationQnaService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestQnaHttpClient();
        service = new QnaService(httpClient).integrations();
    }

    @Test
    void testQuestionGetAnswer() {
        var answer = service.question("q-1").getAnswer("user-123");

        assertEquals("q-1", httpClient.lastQuestionId);
        assertEquals("user-123", httpClient.lastUserId);
        assertSame(httpClient.stubbedAnswer, answer);
    }

    @Test
    void testQuestionGetAnswerWithNullUserId() {
        var answer = service.question("q-1").getAnswer(null);

        assertEquals("q-1", httpClient.lastQuestionId);
        assertNull(httpClient.lastUserId);
        assertSame(httpClient.stubbedAnswer, answer);
    }

    @Test
    void testQuestionResourceId() {
        var resource = service.question("q-1");

        assertEquals("q-1", resource.id());
    }

    @Test
    void testQuestionResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.question(null));
    }

    private static class TestQnaHttpClient extends QnaHttpClient {

        String lastQuestionId;

        String lastUserId;

        final Answer stubbedAnswer = new Answer("Some answer", "agent-id-001", 1, ResponseCompleteness.COMPLETE, null,
                null, "Some question", null, null, null, null);

        public TestQnaHttpClient() {
            super(QnaHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public Answer getIntegrationQuestionAnswer(String questionId, String userId) {
            lastQuestionId = questionId;
            lastUserId = userId;
            return stubbedAnswer;
        }
    }
}
