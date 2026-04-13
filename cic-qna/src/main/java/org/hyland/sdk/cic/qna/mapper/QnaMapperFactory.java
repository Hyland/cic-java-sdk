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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.Answer;
import org.hyland.sdk.cic.qna.object.ContinueConversationRequest;
import org.hyland.sdk.cic.qna.object.Conversation;
import org.hyland.sdk.cic.qna.object.ConversationMessage;
import org.hyland.sdk.cic.qna.object.ConversationPage;
import org.hyland.sdk.cic.qna.object.FeedbackBreakdown;
import org.hyland.sdk.cic.qna.object.MessagePage;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;
import org.hyland.sdk.cic.qna.object.SetAnswerRequest;
import org.hyland.sdk.cic.qna.object.StartConversationRequest;
import org.hyland.sdk.cic.qna.object.StartConversationResponse;
import org.hyland.sdk.cic.qna.object.SubmitFeedbackRequest;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.qna.object.UpdateConversationRequest;

/**
 * @since 1.0.0
 */
public class QnaMapperFactory implements MapperService.MapperFactory {

    private static final List<Entry<Class<?>, CICMapper<?>>> MAPPERS = List.of(
            Map.entry(StartConversationResponse.class, new StartConversationResponseMapper()),
            Map.entry(StartConversationRequest.class, new StartConversationRequestMapper()),
            Map.entry(ContinueConversationRequest.class, new ContinueConversationRequestMapper()),
            Map.entry(UpdateConversationRequest.class, new UpdateConversationRequestMapper()),
            Map.entry(Conversation.class, new ConversationMapper()),
            Map.entry(ConversationMessage.class, new ConversationMessageMapper()),
            Map.entry(ConversationPage.class, new ConversationCursorPageMapper()),
            Map.entry(MessagePage.class, new MessageCursorPageMapper()),
            Map.entry(SubmitQuestionRequest.class, new SubmitQuestionRequestMapper()),
            Map.entry(SetAnswerRequest.class, new SetAnswerRequestMapper()),
            Map.entry(Answer.class, new AnswerMapper()),
            Map.entry(SubmitFeedbackRequest.class, new SubmitFeedbackRequestMapper()),
            Map.entry(QuestionHistoryPage.class, new QuestionHistoryPageMapper()),
            Map.entry(FeedbackBreakdown.class, new FeedbackBreakdownMapper()));

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        for (var entry : MAPPERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return (CICMapper<T>) entry.getValue();
            }
        }
        return null;
    }
}
