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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.http.client.pagination.PageableResponse;
import org.hyland.sdk.cic.http.client.pagination.Pagination;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ContinueConversationRequest;
import org.hyland.sdk.cic.qna.object.Conversation;
import org.hyland.sdk.cic.qna.object.ConversationMessage;
import org.hyland.sdk.cic.qna.object.ConversationPage;
import org.hyland.sdk.cic.qna.object.FeedbackBreakdown;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.IntegrationType;
import org.hyland.sdk.cic.qna.object.MessagePage;
import org.hyland.sdk.cic.qna.object.MessageStatus;
import org.hyland.sdk.cic.qna.object.QuestionHistory;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;
import org.hyland.sdk.cic.qna.object.SetAnswerRequest;
import org.hyland.sdk.cic.qna.object.StartConversationRequest;
import org.hyland.sdk.cic.qna.object.StartConversationResponse;
import org.hyland.sdk.cic.qna.object.SubmitFeedbackRequest;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.qna.object.UpdateConversationRequest;

/**
 * @since 1.0.0
 */
class QnaServiceTest {

    private TestQnaHttpClient httpClient;

    private QnaService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestQnaHttpClient();
        service = new QnaService(httpClient);
    }

    @Test
    void testAgentStartConversation() {
        var conversation = new Conversation("conv-id", "Conv", "Desc", "2026-04-02T11:42:00Z");
        var message = new ConversationMessage("msg-id", "Hello?", "Hi!", List.of(), List.of(), null, null, null,
                "2026-04-02T11:42:00Z", "2026-04-02T11:42:01Z", 1, MessageStatus.ANSWERED);
        httpClient.startConversationResponse = new StartConversationResponse(conversation, message);

        var result = service.agent("agent-1").startConversation(r -> r.question("Hello?"));

        assertNotNull(result);
        assertEquals(conversation, result.conversation());
        assertEquals(message, result.message());
        assertEquals("Hello?", httpClient.lastStartConversationRequest.question());
        assertEquals("agent-1", httpClient.lastAgentId);
    }

    @Test
    void testAgentListConversations() {
        var conversations = List.of(new Conversation("c1", "Conv 1", null, "2026-04-02T11:42:00Z"));
        httpClient.conversationPage = new ConversationPage(conversations, new CursorPagination(null, false));

        var result = service.agent("agent-1").listConversations();

        assertEquals(1, result.data().size());
        assertEquals(conversations.get(0), result.data().get(0));
        assertEquals("agent-1", httpClient.lastAgentId);
    }

    @Test
    void testAgentListConversationsWithPagination() {
        httpClient.conversationPage = new ConversationPage(List.of(), new CursorPagination("next", true));

        service.agent("agent-1").listConversations(r -> r.cursor("cursor-abc").pageSize(10));

        assertEquals("cursor-abc", httpClient.lastCursor);
        assertEquals(10, httpClient.lastPageSize);
    }

    @Test
    void testConversationGet() {
        httpClient.conversation = new Conversation("conv-1", "My Conv", "Desc", "2026-04-02T11:42:00Z");

        var result = service.agent("agent-1").conversation("conv-1").get();

        assertEquals(httpClient.conversation, result);
        assertEquals("agent-1", httpClient.lastAgentId);
        assertEquals("conv-1", httpClient.lastConversationId);
    }

    @Test
    void testConversationUpdate() {
        service.agent("agent-1").conversation("conv-1").update(r -> r.name("Updated"));

        assertEquals("agent-1", httpClient.lastAgentId);
        assertEquals("conv-1", httpClient.lastConversationId);
        assertEquals("Updated", httpClient.lastUpdateConversationRequest.name());
    }

    @Test
    void testConversationSendMessage() {
        httpClient.conversationMessage = new ConversationMessage("msg-2", "Follow up?", "Sure.", List.of(), List.of(),
                null, null, null, "2026-04-02T12:00:00Z", "2026-04-02T12:00:01Z", 1, MessageStatus.ANSWERED);

        var result = service.agent("agent-1").conversation("conv-1").sendMessage(r -> r.question("Follow up?"));

        assertEquals(httpClient.conversationMessage, result);
        assertEquals("Follow up?", httpClient.lastContinueConversationRequest.question());
    }

    @Test
    void testConversationListMessages() {
        var messages = List.of(new ConversationMessage("msg-1", "Q?", "A.", List.of(), List.of(), null, null, null,
                "2026-04-02T11:42:00Z", "2026-04-02T11:42:01Z", 1, MessageStatus.ANSWERED));
        httpClient.messagePage = new MessagePage(messages, new CursorPagination(null, false));

        var result = service.agent("agent-1").conversation("conv-1").listMessages();

        assertEquals(1, result.data().size());
        assertEquals(messages.get(0), result.data().get(0));
    }

    @Test
    void testConversationGetMessage() {
        httpClient.conversationMessage = new ConversationMessage("msg-1", "Q?", "A.", List.of(), List.of(), null, null,
                null, "2026-04-02T11:42:00Z", "2026-04-02T11:42:01Z", 1, MessageStatus.ANSWERED);

        var result = service.agent("agent-1").conversation("conv-1").message("msg-1").get();

        assertEquals(httpClient.conversationMessage, result);
        assertEquals("msg-1", httpClient.lastMessageId);
    }

    @Test
    void testSubmitQuestion() {
        var request = SubmitQuestionRequest.builder()
                                           .questionId("q-1")
                                           .question("What?")
                                           .userId("user-1")
                                           .integrationType(IntegrationType.HX)
                                           .agentId("agent-1")
                                           .modelName("model-1")
                                           .build();

        service.submitQuestion(request);

        assertEquals(request, httpClient.lastSubmitQuestionRequest);
    }

    @Test
    void testSubmitQuestionWithConsumer() {
        service.submitQuestion(r -> r.questionId("q-1")
                                     .question("What?")
                                     .userId("user-1")
                                     .integrationType(IntegrationType.HX)
                                     .agentId("agent-1")
                                     .modelName("model-1"));

        assertNotNull(httpClient.lastSubmitQuestionRequest);
        assertEquals("q-1", httpClient.lastSubmitQuestionRequest.questionId());
    }

    @Test
    void testQuestionSetAnswer() {
        service.question("q-1").setAnswer(r -> r.answer("The answer."));

        assertEquals("q-1", httpClient.lastQuestionId);
        assertEquals("The answer.", httpClient.lastSetAnswerRequest.answer());
    }

    @Test
    void testQuestionGetAnswer() {
        httpClient.answer = new Answer("The answer.", "agent-1", 2, ResponseCompleteness.COMPLETE, null, null, "What?",
                null, null, null, null);

        var result = service.question("q-1").getAnswer();

        assertEquals(httpClient.answer, result);
        assertEquals("q-1", httpClient.lastQuestionId);
    }

    @Test
    void testQuestionMarkError() {
        service.question("q-1").markError();

        assertEquals("q-1", httpClient.lastQuestionId);
        assertEquals("error", httpClient.lastMarkAction);
    }

    @Test
    void testQuestionMarkBlocked() {
        service.question("q-1").markBlocked();

        assertEquals("q-1", httpClient.lastQuestionId);
        assertEquals("blocked", httpClient.lastMarkAction);
    }

    @Test
    void testQuestionSubmitFeedback() {
        service.question("q-1").submitFeedback(FeedbackType.GOOD);

        assertEquals("q-1", httpClient.lastQuestionId);
        assertEquals(FeedbackType.GOOD, httpClient.lastSubmitFeedbackRequest.feedback());
    }

    @Test
    void testAgentGetQuestionHistory() {
        var history = List.of(new QuestionHistory("q-1", "What?", "Answer.", "2026-04-02T11:42:00Z",
                "2026-04-02T11:42:01Z", 1, null, null, null, null));
        httpClient.questionHistoryPage = new QuestionHistoryPage(history, new Pagination(10, 1, 1, 1));

        var result = service.agent("agent-1").getQuestionHistory();

        assertEquals(1, result.data().size());
        assertEquals(history.get(0), result.data().get(0));
    }

    @Test
    void testAgentGetFeedbackBreakdown() {
        httpClient.feedbackBreakdown = new FeedbackBreakdown(10, 3, 2, 85, 100);

        var result = service.agent("agent-1").getFeedbackBreakdown();

        assertEquals(httpClient.feedbackBreakdown, result);
    }

    @Test
    void testAgentResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.agent(null));
    }

    @Test
    void testQuestionResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.question(null));
    }

    @Test
    void testConversationResourceNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.agent("agent-1").conversation(null));
    }

    @Test
    void testConversationMessageNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.agent("agent-1").conversation("conv-1").message(null));
    }

    private static class TestQnaHttpClient extends QnaHttpClient {

        StartConversationResponse startConversationResponse;

        ConversationPage conversationPage;

        Conversation conversation;

        ConversationMessage conversationMessage;

        MessagePage messagePage;

        Answer answer;

        FeedbackBreakdown feedbackBreakdown;

        QuestionHistoryPage questionHistoryPage;

        String lastAgentId;

        String lastConversationId;

        String lastMessageId;

        String lastQuestionId;

        String lastCursor;

        Integer lastPageSize;

        String lastMarkAction;

        StartConversationRequest lastStartConversationRequest;

        UpdateConversationRequest lastUpdateConversationRequest;

        ContinueConversationRequest lastContinueConversationRequest;

        SubmitQuestionRequest lastSubmitQuestionRequest;

        SetAnswerRequest lastSetAnswerRequest;

        SubmitFeedbackRequest lastSubmitFeedbackRequest;

        public TestQnaHttpClient() {
            super(QnaHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public StartConversationResponse startConversation(String agentId, StartConversationRequest request) {
            lastAgentId = agentId;
            lastStartConversationRequest = request;
            return startConversationResponse;
        }

        @Override
        public CursorPageableResponse<Conversation> listConversations(String agentId, String cursor, Integer pageSize) {
            lastAgentId = agentId;
            lastCursor = cursor;
            lastPageSize = pageSize;
            return conversationPage;
        }

        @Override
        public Conversation getConversation(String agentId, String conversationId) {
            lastAgentId = agentId;
            lastConversationId = conversationId;
            return conversation;
        }

        @Override
        public void updateConversation(String agentId, String conversationId, UpdateConversationRequest request) {
            lastAgentId = agentId;
            lastConversationId = conversationId;
            lastUpdateConversationRequest = request;
        }

        @Override
        public ConversationMessage sendMessage(String agentId, String conversationId,
                ContinueConversationRequest request) {
            lastAgentId = agentId;
            lastConversationId = conversationId;
            lastContinueConversationRequest = request;
            return conversationMessage;
        }

        @Override
        public CursorPageableResponse<ConversationMessage> listMessages(String agentId, String conversationId,
                String cursor, Integer pageSize, Integer maxContentLength, String fields) {
            lastAgentId = agentId;
            lastConversationId = conversationId;
            lastCursor = cursor;
            lastPageSize = pageSize;
            return messagePage;
        }

        @Override
        public ConversationMessage getMessage(String agentId, String conversationId, String messageId) {
            lastAgentId = agentId;
            lastConversationId = conversationId;
            lastMessageId = messageId;
            return conversationMessage;
        }

        @Override
        public void submitQuestion(SubmitQuestionRequest request) {
            lastSubmitQuestionRequest = request;
        }

        @Override
        public void setAnswer(String questionId, SetAnswerRequest request) {
            lastQuestionId = questionId;
            lastSetAnswerRequest = request;
        }

        @Override
        public Answer getAnswer(String questionId) {
            lastQuestionId = questionId;
            return answer;
        }

        @Override
        public void markQuestionError(String questionId) {
            lastQuestionId = questionId;
            lastMarkAction = "error";
        }

        @Override
        public void markQuestionBlocked(String questionId) {
            lastQuestionId = questionId;
            lastMarkAction = "blocked";
        }

        @Override
        public void submitFeedback(String questionId, SubmitFeedbackRequest request) {
            lastQuestionId = questionId;
            lastSubmitFeedbackRequest = request;
        }

        @Override
        public PageableResponse<QuestionHistory> getQuestionHistory(String agentId, Integer pageNumber,
                Integer pageSize, Integer maxContentLength) {
            lastAgentId = agentId;
            return questionHistoryPage;
        }

        @Override
        public FeedbackBreakdown getFeedbackBreakdown(String agentId) {
            lastAgentId = agentId;
            return feedbackBreakdown;
        }
    }
}
