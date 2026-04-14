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

import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.toCICObject;
import static org.hyland.sdk.cic.qna.mapper.QnaMapperUtils.writeStringList;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.qna.object.Filters;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;

/**
 * @since 1.0.0
 */
class SubmitQuestionRequestMapper implements CICMapper<SubmitQuestionRequest> {

    @Override
    public CICNode toCICNode(SubmitQuestionRequest request) {
        var obj = CICObject.create();
        obj.putString("questionId", request.questionId());
        obj.putString("question", request.question());
        if (!request.contextObjectIds().isEmpty()) {
            obj.putArray("contextObjectIds", writeStringList(request.contextObjectIds()));
        }
        obj.putString("userId", request.userId());
        if (request.externalUserId() != null) {
            obj.putString("externalUserId", request.externalUserId());
        } else {
            obj.putNull("externalUserId");
        }
        obj.putString("integrationType", request.integrationType().value());
        obj.putString("agentId", request.agentId());
        obj.putInt("agentVersion", request.agentVersion());
        obj.putString("modelName", request.modelName());
        if (request.instructions() != null) {
            obj.putString("instructions", request.instructions());
        }
        if (!request.sourceIds().isEmpty()) {
            obj.putArray("sourceIds", writeStringList(request.sourceIds()));
        }
        if (request.filters() != null) {
            obj.putObject("filters", writeFilters(request.filters()));
        }
        return obj;
    }

    private CICObject writeFilters(Filters filters) {
        var obj = CICObject.create();
        if (filters.merged() != null) {
            obj.putObject("merged", toCICObject(filters.merged()));
        }
        if (filters.dynamic() != null) {
            obj.putObject("dynamic", toCICObject(filters.dynamic()));
        }
        if (filters.staticFilter() != null) {
            obj.putObject("static", toCICObject(filters.staticFilter()));
        }
        if (filters.hxqlFilter() != null) {
            obj.putString("hxqlFilter", filters.hxqlFilter());
        } else {
            obj.putNull("hxqlFilter");
        }
        return obj;
    }
}
