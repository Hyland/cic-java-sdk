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

import java.util.Objects;
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.PageIterable;
import org.hyland.sdk.cic.http.client.pagination.PageableResponse;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ContinueConversationRequest;
import org.hyland.sdk.cic.qna.object.Conversation;
import org.hyland.sdk.cic.qna.object.ConversationMessage;
import org.hyland.sdk.cic.qna.object.FeedbackBreakdown;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.GetQuestionHistoryRequest;
import org.hyland.sdk.cic.qna.object.ListConversationsRequest;
import org.hyland.sdk.cic.qna.object.ListMessagesRequest;
import org.hyland.sdk.cic.qna.object.QuestionHistory;
import org.hyland.sdk.cic.qna.object.SetAnswerRequest;
import org.hyland.sdk.cic.qna.object.StartConversationRequest;
import org.hyland.sdk.cic.qna.object.StartConversationResponse;
import org.hyland.sdk.cic.qna.object.SubmitFeedbackRequest;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.qna.object.UpdateConversationRequest;

/**
 * @since 1.0.0
 */
public class QnaService {

    protected final QnaHttpClient httpClient;

    public QnaService(QnaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns an {@link AgentResource} handle bound to the given agent ID.
     *
     * @param agentId the agent ID
     * @return the agent resource handle
     * @throws NullPointerException if agentId is null
     */
    public AgentResource agent(String agentId) {
        return new AgentResource(httpClient, Objects.requireNonNull(agentId, "agentId cannot be null"));
    }

    /**
     * Returns a {@link QuestionResource} handle bound to the given question ID.
     *
     * @param questionId the question ID
     * @return the question resource handle
     * @throws NullPointerException if questionId is null
     */
    public QuestionResource question(String questionId) {
        return new QuestionResource(httpClient, Objects.requireNonNull(questionId, "questionId cannot be null"));
    }

    /**
     * Returns an {@link IntegrationQnaService} for integration-specific QnA operations backed by the same HTTP client.
     *
     * @return the integration QnA service
     */
    public IntegrationQnaService integrations() {
        return new IntegrationQnaService(httpClient);
    }

    /**
     * Submits a question for processing.
     *
     * @param request the submit question request
     * @throws CICSdkException if the request fails
     */
    public void submitQuestion(SubmitQuestionRequest request) {
        httpClient.submitQuestion(request);
    }

    /**
     * Submits a question for processing using a builder consumer.
     *
     * @param consumer configures the submit question request
     * @throws CICSdkException if the request fails
     */
    public void submitQuestion(Consumer<SubmitQuestionRequest.Builder> consumer) {
        var builder = SubmitQuestionRequest.builder();
        consumer.accept(builder);
        httpClient.submitQuestion(builder.build());
    }

    /**
     * A resource handle bound to a specific agent ID. Provides agent-scoped operations for conversations and question
     * history.
     *
     * @since 1.0.0
     */
    public static class AgentResource {

        private final QnaHttpClient httpClient;

        private final String agentId;

        private AgentResource(QnaHttpClient httpClient, String agentId) {
            this.httpClient = httpClient;
            this.agentId = agentId;
        }

        /**
         * Returns the agent ID this handle is bound to.
         *
         * @return the agent ID
         */
        public String id() {
            return agentId;
        }

        /**
         * Starts a new conversation with this agent.
         *
         * @param request the start conversation request
         * @return the start conversation response
         * @throws CICSdkException if the request fails
         */
        public StartConversationResponse startConversation(StartConversationRequest request) {
            return httpClient.startConversation(agentId, request);
        }

        /**
         * Starts a new conversation with this agent using a builder consumer.
         *
         * @param consumer configures the start conversation request
         * @return the start conversation response
         * @throws CICSdkException if the request fails
         */
        public StartConversationResponse startConversation(Consumer<StartConversationRequest.Builder> consumer) {
            var builder = StartConversationRequest.builder();
            consumer.accept(builder);
            return httpClient.startConversation(agentId, builder.build());
        }

        /**
         * Lists conversations for this agent.
         *
         * @return a cursor-paginated response of conversations
         * @throws CICSdkException if the request fails
         */
        public CursorPageableResponse<Conversation> listConversations() {
            return httpClient.listConversations(agentId, null, null);
        }

        /**
         * Lists conversations for this agent using a builder consumer to specify pagination options.
         *
         * @param consumer configures optional pagination parameters (cursor, pageSize)
         * @return a cursor-paginated response of conversations
         * @throws CICSdkException if the request fails
         */
        public CursorPageableResponse<Conversation> listConversations(
                Consumer<ListConversationsRequest.Builder> consumer) {
            var builder = ListConversationsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listConversations(agentId, request.cursor(), request.pageSize());
        }

        /**
         * Returns a lazily-fetching iterable over all conversations for this agent across all pages.
         *
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public CursorPageIterable<Conversation> listConversationsPaginator() {
            return new CursorPageIterable<>(cursor -> httpClient.listConversations(agentId, cursor, null));
        }

        /**
         * Returns a lazily-fetching iterable over all conversations for this agent across all pages, using a builder
         * consumer to specify pagination options such as page size.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional pagination parameters (pageSize)
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public CursorPageIterable<Conversation> listConversationsPaginator(
                Consumer<ListConversationsRequest.Builder> consumer) {
            var builder = ListConversationsRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(
                    cursor -> httpClient.listConversations(agentId, cursor, request.pageSize()));
        }

        /**
         * Returns a {@link ConversationResource} handle bound to the given conversation ID.
         *
         * @param conversationId the conversation ID
         * @return the conversation resource handle
         * @throws NullPointerException if conversationId is null
         */
        public ConversationResource conversation(String conversationId) {
            return new ConversationResource(httpClient, agentId,
                    Objects.requireNonNull(conversationId, "conversationId cannot be null"));
        }

        /**
         * Gets question history for this agent.
         *
         * @return a paginated response of question history entries
         * @throws CICSdkException if the request fails
         */
        public PageableResponse<QuestionHistory> getQuestionHistory() {
            return httpClient.getQuestionHistory(agentId, null, null, null);
        }

        /**
         * Gets question history for this agent using a builder consumer to specify pagination options.
         *
         * @param consumer configures optional parameters (pageNumber, pageSize, maxContentLength)
         * @return a paginated response of question history entries
         * @throws CICSdkException if the request fails
         */
        public PageableResponse<QuestionHistory> getQuestionHistory(
                Consumer<GetQuestionHistoryRequest.Builder> consumer) {
            var builder = GetQuestionHistoryRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.getQuestionHistory(agentId, request.pageNumber(), request.pageSize(),
                    request.maxContentLength());
        }

        /**
         * Returns a lazily-fetching iterable over all question history entries for this agent across all pages.
         *
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public PageIterable<QuestionHistory> getQuestionHistoryPaginator() {
            return new PageIterable<>(pageNumber -> httpClient.getQuestionHistory(agentId, pageNumber, null, null));
        }

        /**
         * Returns a lazily-fetching iterable over all question history entries for this agent across all pages, using a
         * builder consumer to specify pagination and filter options.
         * <p>
         * The {@code pageNumber} field of the request is ignored; the paginator manages the page number internally.
         *
         * @param consumer configures optional parameters (pageSize, maxContentLength)
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public PageIterable<QuestionHistory> getQuestionHistoryPaginator(
                Consumer<GetQuestionHistoryRequest.Builder> consumer) {
            var builder = GetQuestionHistoryRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new PageIterable<>(pageNumber -> httpClient.getQuestionHistory(agentId, pageNumber,
                    request.pageSize(), request.maxContentLength()));
        }

        /**
         * Gets the feedback breakdown for this agent.
         *
         * @return the feedback breakdown
         * @throws CICSdkException if the request fails
         */
        public FeedbackBreakdown getFeedbackBreakdown() {
            return httpClient.getFeedbackBreakdown(agentId);
        }
    }

    /**
     * A resource handle bound to a specific conversation within an agent.
     *
     * @since 1.0.0
     */
    public static class ConversationResource {

        private final QnaHttpClient httpClient;

        private final String agentId;

        private final String conversationId;

        private ConversationResource(QnaHttpClient httpClient, String agentId, String conversationId) {
            this.httpClient = httpClient;
            this.agentId = agentId;
            this.conversationId = conversationId;
        }

        /**
         * Returns the conversation ID this handle is bound to.
         *
         * @return the conversation ID
         */
        public String id() {
            return conversationId;
        }

        /**
         * Gets this conversation.
         *
         * @return the conversation
         * @throws CICSdkException if the request fails
         */
        public Conversation get() {
            return httpClient.getConversation(agentId, conversationId);
        }

        /**
         * Updates this conversation.
         *
         * @param request the update request
         * @throws CICSdkException if the request fails
         */
        public void update(UpdateConversationRequest request) {
            httpClient.updateConversation(agentId, conversationId, request);
        }

        /**
         * Updates this conversation using a builder consumer.
         *
         * @param consumer configures the update request
         * @throws CICSdkException if the request fails
         */
        public void update(Consumer<UpdateConversationRequest.Builder> consumer) {
            var builder = UpdateConversationRequest.builder();
            consumer.accept(builder);
            httpClient.updateConversation(agentId, conversationId, builder.build());
        }

        /**
         * Sends a message to this conversation.
         *
         * @param request the continue conversation request
         * @return the conversation message response
         * @throws CICSdkException if the request fails
         */
        public ConversationMessage sendMessage(ContinueConversationRequest request) {
            return httpClient.sendMessage(agentId, conversationId, request);
        }

        /**
         * Sends a message to this conversation using a builder consumer.
         *
         * @param consumer configures the continue conversation request
         * @return the conversation message response
         * @throws CICSdkException if the request fails
         */
        public ConversationMessage sendMessage(Consumer<ContinueConversationRequest.Builder> consumer) {
            var builder = ContinueConversationRequest.builder();
            consumer.accept(builder);
            return httpClient.sendMessage(agentId, conversationId, builder.build());
        }

        /**
         * Lists messages in this conversation.
         *
         * @return a cursor-paginated response of messages
         * @throws CICSdkException if the request fails
         */
        public CursorPageableResponse<ConversationMessage> listMessages() {
            return httpClient.listMessages(agentId, conversationId, null, null, null, null);
        }

        /**
         * Lists messages in this conversation using a builder consumer to specify pagination and filter options.
         *
         * @param consumer configures optional parameters (cursor, pageSize, maxContentLength, fields)
         * @return a cursor-paginated response of messages
         * @throws CICSdkException if the request fails
         */
        public CursorPageableResponse<ConversationMessage> listMessages(
                Consumer<ListMessagesRequest.Builder> consumer) {
            var builder = ListMessagesRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return httpClient.listMessages(agentId, conversationId, request.cursor(), request.pageSize(),
                    request.maxContentLength(), request.fields());
        }

        /**
         * Returns a lazily-fetching iterable over all messages in this conversation across all pages.
         *
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public CursorPageIterable<ConversationMessage> listMessagesPaginator() {
            return new CursorPageIterable<>(
                    cursor -> httpClient.listMessages(agentId, conversationId, cursor, null, null, null));
        }

        /**
         * Returns a lazily-fetching iterable over all messages in this conversation across all pages, using a builder
         * consumer to specify filter and pagination options.
         * <p>
         * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
         *
         * @param consumer configures optional parameters (pageSize, maxContentLength, fields)
         * @return an iterable that fetches pages on demand
         * @throws CICSdkException if any page request fails during iteration
         */
        public CursorPageIterable<ConversationMessage> listMessagesPaginator(
                Consumer<ListMessagesRequest.Builder> consumer) {
            var builder = ListMessagesRequest.builder();
            consumer.accept(builder);
            var request = builder.build();
            return new CursorPageIterable<>(cursor -> httpClient.listMessages(agentId, conversationId, cursor,
                    request.pageSize(), request.maxContentLength(), request.fields()));
        }

        /**
         * Returns a {@link MessageResource} handle bound to the given message ID.
         *
         * @param messageId the message ID
         * @return the message resource handle
         * @throws NullPointerException if messageId is null
         */
        public MessageResource message(String messageId) {
            return new MessageResource(httpClient, agentId, conversationId,
                    Objects.requireNonNull(messageId, "messageId cannot be null"));
        }
    }

    /**
     * A resource handle bound to a specific message within a conversation.
     *
     * @since 1.0.0
     */
    public static class MessageResource {

        private final QnaHttpClient httpClient;

        private final String agentId;

        private final String conversationId;

        private final String messageId;

        private MessageResource(QnaHttpClient httpClient, String agentId, String conversationId, String messageId) {
            this.httpClient = httpClient;
            this.agentId = agentId;
            this.conversationId = conversationId;
            this.messageId = messageId;
        }

        /**
         * Returns the message ID this handle is bound to.
         *
         * @return the message ID
         */
        public String id() {
            return messageId;
        }

        /**
         * Gets this message.
         *
         * @return the conversation message
         * @throws CICSdkException if the request fails
         */
        public ConversationMessage get() {
            return httpClient.getMessage(agentId, conversationId, messageId);
        }
    }

    /**
     * A resource handle bound to a specific question ID. Provides question-scoped operations for answers and feedback.
     *
     * @since 1.0.0
     */
    public static class QuestionResource {

        private final QnaHttpClient httpClient;

        private final String questionId;

        private QuestionResource(QnaHttpClient httpClient, String questionId) {
            this.httpClient = httpClient;
            this.questionId = questionId;
        }

        /**
         * Returns the question ID this handle is bound to.
         *
         * @return the question ID
         */
        public String id() {
            return questionId;
        }

        /**
         * Sets the answer for this question.
         *
         * @param request the set answer request
         * @throws CICSdkException if the request fails
         */
        public void setAnswer(SetAnswerRequest request) {
            httpClient.setAnswer(questionId, request);
        }

        /**
         * Sets the answer for this question using a builder consumer.
         *
         * @param consumer configures the set answer request
         * @throws CICSdkException if the request fails
         */
        public void setAnswer(Consumer<SetAnswerRequest.Builder> consumer) {
            var builder = SetAnswerRequest.builder();
            consumer.accept(builder);
            httpClient.setAnswer(questionId, builder.build());
        }

        /**
         * Gets the answer for this question.
         *
         * @return the answer
         * @throws CICSdkException if the request fails
         */
        public Answer getAnswer() {
            return httpClient.getAnswer(questionId);
        }

        /**
         * Marks this question as having an error.
         *
         * @throws CICSdkException if the request fails
         */
        public void markError() {
            httpClient.markQuestionError(questionId);
        }

        /**
         * Marks this question as blocked.
         *
         * @throws CICSdkException if the request fails
         */
        public void markBlocked() {
            httpClient.markQuestionBlocked(questionId);
        }

        /**
         * Submits feedback for this question's answer.
         *
         * @param feedback the feedback type
         * @throws CICSdkException if the request fails
         */
        public void submitFeedback(FeedbackType feedback) {
            httpClient.submitFeedback(questionId, new SubmitFeedbackRequest(feedback));
        }

        /**
         * Submits feedback for this question's answer.
         *
         * @param request the feedback request
         * @throws CICSdkException if the request fails
         */
        public void submitFeedback(SubmitFeedbackRequest request) {
            httpClient.submitFeedback(questionId, request);
        }
    }
}
