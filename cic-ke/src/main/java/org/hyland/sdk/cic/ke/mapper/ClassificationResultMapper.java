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

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.ClassificationResult;

/**
 * @since 1.1.0
 */
class ClassificationResultMapper {

    ClassificationResult fromCICObject(CICObject resultObj) {
        var classification = resultObj.getStringOrNull("classification");
        var confidenceNode = resultObj.getProperties().get("confidence");
        double confidence = 0.0;
        if (confidenceNode instanceof CICPrimitive.CICDouble d) {
            confidence = d.value();
        } else if (confidenceNode instanceof CICPrimitive.CICInt i) {
            confidence = (double) i.value();
        } else if (confidenceNode instanceof CICPrimitive.CICLong l) {
            confidence = (double) l.value();
        }
        return new ClassificationResult(classification, confidence);
    }
}
