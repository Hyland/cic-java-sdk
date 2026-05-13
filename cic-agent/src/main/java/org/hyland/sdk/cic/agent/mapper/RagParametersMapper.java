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

import org.hyland.sdk.cic.agent.object.RagParameters;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
class RagParametersMapper implements CICMapper<RagParameters> {

    @Override
    public RagParameters fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new RagParameters(obj.getIntegerOrNull("limit"), obj.getIntegerOrNull("adjacentChunkRange"),
                obj.getBooleanOrNull("adjacentChunkMerge"), obj.getBooleanOrNull("rerankerEnabled"),
                obj.getIntegerOrNull("rerankerTopN"));
    }

    @Override
    public CICObject toCICNode(RagParameters ragParameters) {
        var obj = CICObject.create();
        if (ragParameters.limit() != null) {
            obj.putInt("limit", ragParameters.limit());
        }
        if (ragParameters.adjacentChunkRange() != null) {
            obj.putInt("adjacentChunkRange", ragParameters.adjacentChunkRange());
        }
        if (ragParameters.adjacentChunkMerge() != null) {
            obj.putBoolean("adjacentChunkMerge", ragParameters.adjacentChunkMerge());
        }
        if (ragParameters.rerankerEnabled() != null) {
            obj.putBoolean("rerankerEnabled", ragParameters.rerankerEnabled());
        }
        if (ragParameters.rerankerTopN() != null) {
            obj.putInt("rerankerTopN", ragParameters.rerankerTopN());
        }
        return obj;
    }
}
