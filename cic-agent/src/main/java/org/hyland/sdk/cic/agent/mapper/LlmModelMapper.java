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
package org.hyland.sdk.cic.agent.mapper;

import java.util.stream.Collectors;

import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class LlmModelMapper implements CICMapper<LlmModel> {

    @Override
    public LlmModel fromCICNode(CICNode cicNode) {
        var obj = (CICObject) cicNode;
        return new LlmModel(obj.getStringOrThrow("displayName"), obj.getStringOrThrow("modelName"),
                obj.getStringOrThrow("status"), obj.getString("eolDate", null),
                obj.getString("replacementModelName", null));
    }

    static class ListMapper implements CICMapper<LlmModel.List> {

        private final LlmModelMapper innerMapper = new LlmModelMapper();

        @Override
        public LlmModel.List fromCICNode(CICNode cicNode) {
            var cicArray = (CICArray) cicNode;
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(LlmModel.List::new));
        }
    }
}
