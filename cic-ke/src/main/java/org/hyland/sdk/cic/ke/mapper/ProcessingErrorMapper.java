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

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.ProcessingError;
import org.hyland.sdk.cic.ke.object.ProcessingErrorType;

/**
 * @since 1.1.0
 */
class ProcessingErrorMapper {

    ProcessingError mapActionError(CICObject actionObj) {
        var errorNode = actionObj.getProperties().get("error");
        if (errorNode != null && !(errorNode instanceof CICPrimitive.CICNull)) {
            if (errorNode instanceof CICObject errorObj) {
                return new ProcessingError(ProcessingErrorType.fromValue(errorObj.getStringOrNull("errorType")),
                        errorObj.getStringOrNull("message"));
            }
            if (errorNode instanceof CICPrimitive.CICString errorStr) {
                return new ProcessingError(ProcessingErrorType.UNKNOWN, errorStr.value());
            }
        }
        var errorMessageNode = actionObj.getProperties().get("errorMessage");
        if (errorMessageNode instanceof CICPrimitive.CICString msgStr) {
            return new ProcessingError(ProcessingErrorType.UNKNOWN, msgStr.value());
        }
        return null;
    }

    List<ProcessingError> mapGeneralProcessingErrors(CICObject parent) {
        var errors = new ArrayList<ProcessingError>();
        parent.getOptionalArray("generalProcessingErrors").ifPresent(array -> {
            for (var error : array.toListObject()) {
                errors.add(new ProcessingError(ProcessingErrorType.fromValue(error.getStringOrNull("errorType")),
                        error.getStringOrNull("message")));
            }
        });
        return List.copyOf(errors);
    }
}
