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
import org.hyland.sdk.cic.ke.object.ActionConfig;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * Serializes {@link ProcessRequest} to Context API v2 JSON format where actions are structured as an object map with
 * per-action configuration.
 *
 * @since 1.1.0
 */
class ProcessRequestMapper implements CICMapper<ProcessRequest> {

    @Override
    public CICNode toCICNode(ProcessRequest request) {
        var cicObject = CICObject.create();

        cicObject.putString("version", request.version());

        var objectKeysArray = CICArray.create();
        for (var objectKey : request.objectKeys()) {
            var keyObj = CICObject.create();
            if (objectKey.path() != null) {
                keyObj.putString("path", objectKey.path());
            } else {
                keyObj.putString("documentId", objectKey.documentId());
            }
            objectKeysArray.addObject(keyObj);
        }
        cicObject.putArray("objectKeys", objectKeysArray);

        var actionsObj = CICObject.create();
        for (var entry : request.actions().entrySet()) {
            actionsObj.putObject(entry.getKey(), serializeActionConfig(entry.getValue()));
        }
        cicObject.putObject("actions", actionsObj);

        if (request.saveResultInContentLakeRepository()) {
            cicObject.putBoolean("saveResultInContentLakeRepository", true);
        }

        return cicObject;
    }

    private CICObject serializeActionConfig(ActionConfig config) {
        var configObj = CICObject.create();

        if (!config.classes().isEmpty()) {
            var classesArr = CICArray.create();
            for (var clazz : config.classes()) {
                classesArr.addString(clazz);
            }
            configObj.putArray("classes", classesArr);
        }

        if (config.maxWordCount() != null) {
            configObj.putInt("maxWordCount", config.maxWordCount());
        }

        if (!config.kSimilarMetadata().isEmpty()) {
            var metadataArr = CICArray.create();
            for (var metadata : config.kSimilarMetadata()) {
                metadataArr.addObject(CICObject.from(metadata));
            }
            configObj.putArray("kSimilarMetadata", metadataArr);
        }

        if (!config.instructions().isEmpty()) {
            var instrObj = CICObject.create();
            for (var instrEntry : config.instructions().entrySet()) {
                instrObj.putString(instrEntry.getKey(), instrEntry.getValue());
            }
            configObj.putObject("instructions", instrObj);
        }

        if (config.category() != null) {
            configObj.putString("category", config.category());
        }

        if (config.model() != null) {
            configObj.putString("model", config.model());
        }

        return configObj;
    }
}
