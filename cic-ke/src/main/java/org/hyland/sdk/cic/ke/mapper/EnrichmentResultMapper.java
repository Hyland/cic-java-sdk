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

import java.util.List;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;

/**
 * @since 1.1.0
 */
class EnrichmentResultMapper implements CICMapper<EnrichmentResult> {

    private final EnrichmentResultEntryMapper entryMapper = new EnrichmentResultEntryMapper();

    @Override
    public EnrichmentResult fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var id = cicObject.getStringOrThrow("id");
        var timestamp = cicObject.getStringOrNull("timestamp");
        var status = cicObject.getStringOrNull("status");
        var inProgress = cicObject.getBoolean("inProgress", false);

        var results = cicObject.getOptionalArray("results")
                               .map(arr -> arr.toListObject().stream().map(entryMapper::fromCICObject).toList())
                               .orElseGet(List::of);

        return new EnrichmentResult(id, timestamp, results, status, inProgress);
    }
}
