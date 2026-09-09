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

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;

/**
 * @since 1.1.0
 */
class PresignResponseMapper implements CICMapper<PresignResponse> {

    private final ProcessingOptionsMapper optionsMapper = new ProcessingOptionsMapper();

    @Override
    public PresignResponse fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var jobId = cicObject.getStringOrThrow("job_id");
        var putUrl = cicObject.getStringOrThrow("put_url");
        var getUrl = cicObject.getStringOrThrow("get_url");
        ProcessingOptions options = optionsMapper.fromCICNode(cicObject.getObjectOrThrow("options"));
        return new PresignResponse(jobId, putUrl, getUrl, options);
    }
}
