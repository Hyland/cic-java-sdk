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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.mapper;

import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;

/**
 * @since 1.0.0
 */
class EmbeddingModelMapper implements CICMapper<EmbeddingModel> {

    @Override
    public EmbeddingModel fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var id = cicObject.getStringOrThrow("id");
        var name = cicObject.getString("name", id);
        return new EmbeddingModel(id, name);
    }

    static class ListMapper implements CICMapper<EmbeddingModel.ListOf> {

        protected final EmbeddingModelMapper innerMapper = new EmbeddingModelMapper();

        @Override
        public EmbeddingModel.ListOf fromCICNode(CICNode cicNode) {
            var cicArray = (CICArray) cicNode;
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(EmbeddingModel.ListOf::new));
        }
    }
}
