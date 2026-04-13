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

import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.readFeedbackTypeOrNull;
import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.readFilterExpressionOrNull;
import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.readPagination;
import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.readResponseCompletenessOrNull;
import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.readStringOrNull;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.qna.object.QuestionHistory;
import org.hyland.sdk.cic.qna.object.QuestionHistoryPage;

/**
 * @since 1.0.0
 */
class QuestionHistoryPageMapper implements CICMapper<QuestionHistoryPage> {

    @Override
    public QuestionHistoryPage fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var dataNode = obj.getProperties().get("data");
        if (!(dataNode instanceof CICArray dataArray)) {
            throw new IllegalArgumentException("Expected CICArray for data");
        }
        var data = dataArray.toListObject().stream().map(this::readQuestionHistory).toList();
        var pagination = readPagination(obj);
        return new QuestionHistoryPage(data, pagination);
    }

    private QuestionHistory readQuestionHistory(CICObject obj) {
        return new QuestionHistory(obj.getStringOrThrow("id"), obj.getStringOrThrow("question"),
                readStringOrNull(obj, "answer"), readStringOrNull(obj, "dateCreated"),
                readStringOrNull(obj, "dateAnswered"), obj.getInt("agentVersion", 0),
                readResponseCompletenessOrNull(obj, "responseCompleteness"), readFeedbackTypeOrNull(obj, "feedback"),
                readFilterExpressionOrNull(obj, "staticFilter"), readFilterExpressionOrNull(obj, "dynamicFilter"));
    }
}
