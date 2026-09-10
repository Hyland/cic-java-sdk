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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;

/**
 * @since 1.1.0
 */
class EmbeddingModelMapper implements CICMapper<EmbeddingModel> {

    @Override
    public EmbeddingModel fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var name = cicObject.getStringOrThrow("name");
        var maxChunkSize = cicObject.getInt("max_chunk_size", 0);

        List<String> supportedPrecisions = new ArrayList<>();
        cicObject.getOptionalArray("supported_precisions").ifPresent(arr -> {
            for (var item : arr.toListString()) {
                supportedPrecisions.add(item);
            }
        });

        List<Integer> supportedOutputDimensions = new ArrayList<>();
        cicObject.getOptionalArray("supported_output_dimensions").ifPresent(arr -> {
            for (var element : arr.getElements()) {
                if (element instanceof CICPrimitive.CICInt i) {
                    supportedOutputDimensions.add(i.value());
                } else if (element instanceof CICPrimitive.CICLong l) {
                    supportedOutputDimensions.add(Math.toIntExact(l.value()));
                }
            }
        });

        List<String> supportedInputType = new ArrayList<>();
        cicObject.getOptionalArray("supported_input_type").ifPresent(arr -> {
            for (var item : arr.toListString()) {
                supportedInputType.add(item);
            }
        });

        return new EmbeddingModel(name, maxChunkSize, supportedPrecisions, supportedOutputDimensions,
                supportedInputType);
    }

    static class ListMapper implements CICMapper<EmbeddingModel.ListOf> {

        protected final EmbeddingModelMapper innerMapper = new EmbeddingModelMapper();

        @Override
        public EmbeddingModel.ListOf fromCICNode(CICNode cicNode) {
            CICArray modelsArray;
            if (cicNode instanceof CICObject cicObject) {
                modelsArray = cicObject.getArrayOrThrow("models");
            } else {
                modelsArray = (CICArray) cicNode;
            }
            return modelsArray.toListObject()
                              .stream()
                              .map(innerMapper::fromCICNode)
                              .collect(Collectors.toCollection(EmbeddingModel.ListOf::new));
        }
    }
}
