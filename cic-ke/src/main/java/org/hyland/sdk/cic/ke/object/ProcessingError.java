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
package org.hyland.sdk.cic.ke.object;

/**
 * Describes a document-level processing error returned by the Context API.
 *
 * @param errorType the category of the processing error
 * @param message the human-readable error description
 * @since 1.0.0
 */
public record ProcessingError(String errorType, String message) {

    /**
     * Returns the known {@link ProcessingErrorType} for this error, or {@link ProcessingErrorType#UNKNOWN} if the type
     * is null or unrecognized.
     */
    public ProcessingErrorType knownType() {
        return ProcessingErrorType.fromValue(errorType);
    }
}
