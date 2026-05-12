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

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.qna.object.FilterExpression;

/**
 * @since 1.0.0
 */
abstract class AbstractConversationRequestMapper<T> implements CICMapper<T> {

    private final FilterExpressionMapper filterExpressionMapper = new FilterExpressionMapper();

    protected CICObject toCICObject(String question, List<String> contextObjectIds, FilterExpression dynamicFilter) {
        var obj = CICObject.create();
        obj.putString("question", question);
        if (!contextObjectIds.isEmpty()) {
            obj.putArray("contextObjectIds", CICArray.from(contextObjectIds));
        }
        if (dynamicFilter != null) {
            obj.putObject("dynamicFilter", filterExpressionMapper.toCICNode(dynamicFilter));
        }
        return obj;
    }
}
