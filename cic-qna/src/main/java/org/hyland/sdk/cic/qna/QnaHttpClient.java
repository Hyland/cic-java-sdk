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

import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.GET;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.POST;
import static org.hyland.sdk.cic.http.client.base.CICHttpRequest.PUT;

import java.util.Objects;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClient;
import org.hyland.sdk.cic.http.client.auth.AbstractAuthenticatedHttpClientBuilder;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.base.CICHttpRequest.CICEntity;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.PageableResponse;
import org.hyland.sdk.cic.http.client.util.ErrorUtils;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ContinueConversationRequest;
import org.hyland.sdk.cic.qna.object.Conversation;
import org.hyland.sdk.cic.qna.object.ConversationMessage;
import org.hyland.sdk.cic.qna.object.ConversationPage;
import org.hyland.sdk.cic.qna.object.FeedbackBreakdown;
import org.hyland.sdk.cic.qna.object.MessagePage;
import org.hyland.sdk.cic.qna.object.QuestionHistory;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;
import org.hyland.sdk.cic.qna.object.SetAnswerRequest;
import org.hyland.sdk.cic.qna.object.StartConversationRequest;
import org.hyland.sdk.cic.qna.object.StartConversationResponse;
import org.hyland.sdk.cic.qna.object.SubmitFeedbackRequest;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.qna.object.UpdateConversationRequest;

/**
 * HTTP client for interacting with the CIC Question and Answer API.
 *
 * @since 1.0.0
 */
public class QnaHttpClient extends AbstractAuthenticatedHttpClient {

    private static final String AGENTS_PATH = "/agents";

    private static final String QUESTIONS_PATH = "/questions";

    private static final String INTEGRATIONS_PATH = "/integrations";

    protected QnaHttpClient(Builder builder) {
        super(builder);
    }

    public static Builder from() {
        // TODO turn this to production
        return from("https://discovery.dev.experience.hyland.com/qna");
    }

    public static Builder from(String baseUrl) {
        return from(baseUrl, AuthenticationHttpClient.from());
    }

    public static Builder from(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
        return new Builder(baseUrl, authenticationBuilder);
    }

    /**
     * Starts a new conversation with an agent.
     *
     * @param agentId the agent ID
     * @param request the start conversation request
     * @return the start conversation response containing the conversation and first message
     * @throws CICSdkException if the request fails
     */
    public StartConversationResponse startConversation(String agentId, StartConversationRequest request) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var httpRequest = this.requestBuilder(POST, AGENTS_PATH + "/" + agentId + "/conversations")
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        return sendThenMapAs(httpRequest, StartConversationResponse.class);
    }

    /**
     * Lists conversations for an agent with cursor-based pagination.
     *
     * @param agentId the agent ID
     * @param cursor optional cursor for pagination (may be null)
     * @param pageSize optional page size (may be null, defaults to 20)
     * @return a cursor-paginated response of conversations
     * @throws CICSdkException if the request fails
     */
    public CursorPageableResponse<Conversation> listConversations(String agentId, String cursor, Integer pageSize) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var requestBuilder = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/conversations");
        if (cursor != null) {
            requestBuilder.queryParameter("cursor", cursor);
        }
        if (pageSize != null) {
            requestBuilder.queryParameter("pageSize", String.valueOf(pageSize));
        }
        return sendThenMapAs(requestBuilder.build(), ConversationPage.class);
    }

    /**
     * Gets a conversation by ID.
     *
     * @param agentId the agent ID
     * @param conversationId the conversation ID
     * @return the conversation
     * @throws CICSdkException if the request fails
     */
    public Conversation getConversation(String agentId, String conversationId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        var request = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/conversations/" + conversationId)
                          .build();
        return sendThenMapAs(request, Conversation.class);
    }

    /**
     * Updates a conversation.
     *
     * @param agentId the agent ID
     * @param conversationId the conversation ID
     * @param request the update request
     * @throws CICSdkException if the request fails
     */
    public void updateConversation(String agentId, String conversationId, UpdateConversationRequest request) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        var httpRequest = this.requestBuilder(PUT, AGENTS_PATH + "/" + agentId + "/conversations/" + conversationId)
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        var response = sendThenReadAsString(httpRequest);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
    }

    /**
     * Sends a message to continue a conversation.
     *
     * @param agentId the agent ID
     * @param conversationId the conversation ID
     * @param request the continue conversation request
     * @return the conversation message response
     * @throws CICSdkException if the request fails
     */
    public ConversationMessage sendMessage(String agentId, String conversationId, ContinueConversationRequest request) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        var httpRequest = this.requestBuilder(POST,
                AGENTS_PATH + "/" + agentId + "/conversations/" + conversationId + "/messages")
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        return sendThenMapAs(httpRequest, ConversationMessage.class);
    }

    /**
     * Lists messages in a conversation with cursor-based pagination.
     *
     * @param agentId the agent ID
     * @param conversationId the conversation ID
     * @param cursor optional cursor for pagination (may be null)
     * @param pageSize optional page size (may be null, defaults to 100)
     * @param maxContentLength optional max content length (may be null)
     * @param fields optional fields filter (may be null)
     * @return a cursor-paginated response of messages
     * @throws CICSdkException if the request fails
     */
    public CursorPageableResponse<ConversationMessage> listMessages(String agentId, String conversationId,
            String cursor, Integer pageSize, Integer maxContentLength, String fields) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        var requestBuilder = this.requestBuilder(GET,
                AGENTS_PATH + "/" + agentId + "/conversations/" + conversationId + "/messages");
        if (cursor != null) {
            requestBuilder.queryParameter("cursor", cursor);
        }
        if (pageSize != null) {
            requestBuilder.queryParameter("pageSize", String.valueOf(pageSize));
        }
        if (maxContentLength != null) {
            requestBuilder.queryParameter("maxContentLength", String.valueOf(maxContentLength));
        }
        if (fields != null) {
            requestBuilder.queryParameter("fields", fields);
        }
        return sendThenMapAs(requestBuilder.build(), MessagePage.class);
    }

    /**
     * Gets a specific message from a conversation.
     *
     * @param agentId the agent ID
     * @param conversationId the conversation ID
     * @param messageId the message ID
     * @return the conversation message
     * @throws CICSdkException if the request fails
     */
    public ConversationMessage getMessage(String agentId, String conversationId, String messageId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        Objects.requireNonNull(messageId, "messageId cannot be null");
        var request = this.requestBuilder(GET,
                AGENTS_PATH + "/" + agentId + "/conversations/" + conversationId + "/messages/" + messageId).build();
        return sendThenMapAs(request, ConversationMessage.class);
    }

    /**
     * Submits a question for processing.
     *
     * @param request the submit question request
     * @throws CICSdkException if the request fails
     */
    public void submitQuestion(SubmitQuestionRequest request) {
        var httpRequest = this.requestBuilder(POST, QUESTIONS_PATH)
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        var response = sendThenReadAsString(httpRequest);
        if (response.statusCode() != 202) {
            ErrorUtils.throwException(response,
                    "Failed to submit question, HTTP response returned with status code: " + response.statusCode());
        }
    }

    /**
     * Sets the answer for a question.
     *
     * @param questionId the question ID
     * @param request the set answer request
     * @throws CICSdkException if the request fails
     */
    public void setAnswer(String questionId, SetAnswerRequest request) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var httpRequest = this.requestBuilder(POST, QUESTIONS_PATH + "/" + questionId + "/answer")
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        var response = sendThenReadAsString(httpRequest);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
    }

    /**
     * Gets the answer for a question.
     *
     * @param questionId the question ID
     * @return the answer
     * @throws CICSdkException if the request fails
     */
    public Answer getAnswer(String questionId) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var request = this.requestBuilder(GET, QUESTIONS_PATH + "/" + questionId + "/answer").build();
        return sendThenMapAs(request, Answer.class);
    }

    /**
     * Marks a question as having an error.
     *
     * @param questionId the question ID
     * @throws CICSdkException if the request fails
     */
    public void markQuestionError(String questionId) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var request = this.requestBuilder(PUT, QUESTIONS_PATH + "/" + questionId + "/error").build();
        var response = sendThenReadAsString(request);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
    }

    /**
     * Marks a question as blocked.
     *
     * @param questionId the question ID
     * @throws CICSdkException if the request fails
     */
    public void markQuestionBlocked(String questionId) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var request = this.requestBuilder(PUT, QUESTIONS_PATH + "/" + questionId + "/blocked").build();
        var response = sendThenReadAsString(request);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
    }

    /**
     * Submits feedback for a question's answer.
     *
     * @param questionId the question ID
     * @param request the feedback request
     * @throws CICSdkException if the request fails
     */
    public void submitFeedback(String questionId, SubmitFeedbackRequest request) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var httpRequest = this.requestBuilder(POST, QUESTIONS_PATH + "/" + questionId + "/answer/feedback")
                              .header("Content-Type", "application/json")
                              .entity(new CICEntity(request))
                              .build();
        var response = sendThenReadAsString(httpRequest);
        ErrorUtils.throwExceptionOnUnexpectedStatusCode(response);
    }

    /**
     * Gets question history for an agent with page-based pagination.
     *
     * @param agentId the agent ID
     * @param pageNumber optional page number (may be null, defaults to 1)
     * @param pageSize optional page size (may be null, defaults to 100)
     * @param maxContentLength optional max content length (may be null)
     * @return a paginated response of question history entries
     * @throws CICSdkException if the request fails
     */
    public PageableResponse<QuestionHistory> getQuestionHistory(String agentId, Integer pageNumber, Integer pageSize,
            Integer maxContentLength) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var requestBuilder = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/questions/history");
        if (pageNumber != null) {
            requestBuilder.queryParameter("pageNumber", String.valueOf(pageNumber));
        }
        if (pageSize != null) {
            requestBuilder.queryParameter("pageSize", String.valueOf(pageSize));
        }
        if (maxContentLength != null) {
            requestBuilder.queryParameter("maxContentLength", String.valueOf(maxContentLength));
        }
        return sendThenMapAs(requestBuilder.build(), QuestionHistoryPage.class);
    }

    /**
     * Gets the feedback breakdown for an agent.
     *
     * @param agentId the agent ID
     * @return the feedback breakdown
     * @throws CICSdkException if the request fails
     */
    public FeedbackBreakdown getFeedbackBreakdown(String agentId) {
        Objects.requireNonNull(agentId, "agentId cannot be null");
        var request = this.requestBuilder(GET, AGENTS_PATH + "/" + agentId + "/feedback-breakdown").build();
        return sendThenMapAs(request, FeedbackBreakdown.class);
    }

    /**
     * Gets the answer for a question via the integrations endpoint.
     *
     * @param questionId the question ID
     * @param userId optional user ID (may be null)
     * @return the answer
     * @throws CICSdkException if the request fails
     */
    public Answer getIntegrationQuestionAnswer(String questionId, String userId) {
        Objects.requireNonNull(questionId, "questionId cannot be null");
        var requestBuilder = this.requestBuilder(GET, INTEGRATIONS_PATH + "/questions/" + questionId + "/answer");
        if (userId != null) {
            requestBuilder.queryParameter("userId", userId);
        }
        return sendThenMapAs(requestBuilder.build(), Answer.class);
    }

    public static class Builder extends AbstractAuthenticatedHttpClientBuilder<Builder, QnaHttpClient> {

        /**
         * Creates a builder for QnaHttpClient.
         *
         * @param baseUrl the base URL of the CIC QnA API
         * @param authenticationBuilder the authentication builder
         */
        public Builder(String baseUrl, AuthenticationHttpClient.Builder authenticationBuilder) {
            super(baseUrl, authenticationBuilder);
            header("Accept", "application/json");
        }

        /**
         * Sets the hxp-environment header.
         *
         * @param environment the environment value
         * @return this builder
         */
        public Builder hxpEnvironment(String environment) {
            return header("hxp-environment", environment);
        }

        /**
         * Sets the hxp-app header.
         *
         * @param app the app value
         * @return this builder
         */
        public Builder hxpApp(String app) {
            return header("hxp-app", app);
        }

        @Override
        public QnaHttpClient build() {
            return new QnaHttpClient(this);
        }
    }
}
