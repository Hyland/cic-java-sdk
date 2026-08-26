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
 * Known processing error types from the Context API. Use {@link #fromValue(String)} for forward-compatible parsing that
 * returns {@link #UNKNOWN} for unrecognized values.
 *
 * @since 1.0.0
 */
public enum ProcessingErrorType {
    LAMBDA_ERROR("LambdaError"), TIMEOUT("Timeout"), VALIDATION_ERROR("ValidationError"), LAMBDA_RESPONSE(
            "LambdaResponse"), UNEXPECTED_ERROR("UnexpectedError"), AUTHORIZATION_ERROR(
                    "AuthorizationError"), DESERIALIZATION_ERROR(
                            "DeserializationError"), GUARDRAIL_VIOLATION("GuardrailViolation"), UNKNOWN(null);

    private final String value;

    ProcessingErrorType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ProcessingErrorType fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (var type : values()) {
            if (type != UNKNOWN && type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
