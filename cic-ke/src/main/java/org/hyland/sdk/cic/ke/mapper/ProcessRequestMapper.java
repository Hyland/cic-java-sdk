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
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * @since 1.0.0
 */
class ProcessRequestMapper implements CICMapper<ProcessRequest> {

    @Override
    public CICNode toCICNode(ProcessRequest request) {
        var cicObject = CICObject.create();

        var objectKeysArray = CICArray.create();
        for (var objectKey : request.objectKeys()) {
            var keyObj = CICObject.create();
            keyObj.putString("path", objectKey.path());
            objectKeysArray.addObject(keyObj);
        }
        cicObject.putArray("objectKeys", objectKeysArray);

        var actionsArray = CICArray.create();
        for (var action : request.actions()) {
            actionsArray.addString(action);
        }
        cicObject.putArray("actions", actionsArray);

        if (request.classes() != null) {
            var classesArray = CICArray.create();
            for (var clazz : request.classes()) {
                classesArray.addString(clazz);
            }
            cicObject.putArray("classes", classesArray);
        }

        if (request.kSimilarMetadata() != null) {
            var metadataArray = CICArray.create();
            for (var metadata : request.kSimilarMetadata()) {
                metadataArray.addObject(CICObject.from(metadata));
            }
            cicObject.putArray("kSimilarMetadata", metadataArray);
        }

        if (request.maxWordCount() != null) {
            cicObject.putInt("maxWordCount", request.maxWordCount());
        }

        if (request.instructions() != null) {
            cicObject.putString("instructions", request.instructions());
        }

        return cicObject;
    }
}
