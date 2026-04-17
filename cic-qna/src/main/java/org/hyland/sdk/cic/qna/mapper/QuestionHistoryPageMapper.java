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

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.pagination.Pagination;
import org.hyland.sdk.cic.qna.object.FeedbackType;
import org.hyland.sdk.cic.qna.object.QuestionHistory;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;
import org.hyland.sdk.cic.qna.object.ResponseCompleteness;

/**
 * @since 1.0.0
 */
class QuestionHistoryPageMapper implements CICMapper<QuestionHistoryPage> {

    private final FilterExpressionMapper filterExpressionMapper = new FilterExpressionMapper();

    @Override
    public QuestionHistoryPage fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var data = obj.getArrayOrThrow("data").toListObject().stream().map(this::readQuestionHistory).toList();
        var pagination = Pagination.from(obj);
        return new QuestionHistoryPage(data, pagination);
    }

    private QuestionHistory readQuestionHistory(CICObject obj) {
        return new QuestionHistory( //
                obj.getStringOrThrow("id"), //
                obj.getStringOrThrow("question"), //
                obj.getStringOrNull("answer"), //
                obj.getStringOrNull("dateCreated"), //
                obj.getStringOrNull("dateAnswered"), //
                obj.getInt("agentVersion", 0), //
                obj.getOptionalString("responseCompleteness").map(ResponseCompleteness::fromValue).orElse(null), //
                obj.getOptionalString("feedback").map(FeedbackType::fromValue).orElse(null), //
                obj.getOptionalObject("staticFilter").map(filterExpressionMapper::fromCICNode).orElse(null), //
                obj.getOptionalObject("dynamicFilter").map(filterExpressionMapper::fromCICNode).orElse(null));
    }
}
