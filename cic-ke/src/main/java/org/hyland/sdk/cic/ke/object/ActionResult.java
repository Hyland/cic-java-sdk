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
 * Represents the outcome of a single enrichment action. Each action in the enrichment response is either a
 * {@link Success} containing the typed result, or a {@link Failure} containing a {@link ProcessingError}.
 *
 * @param <T> the type of the successful result
 * @since 1.1.0
 */
public sealed interface ActionResult<T> permits ActionResult.Success, ActionResult.Failure {

    static <T> ActionResult<T> success(T result) {
        return new Success<>(result);
    }

    static <T> ActionResult<T> failure(ProcessingError error) {
        return new Failure<>(error);
    }

    boolean isSuccess();

    /**
     * Returns the result value if this is a {@link Success}, or {@code null} if this is a {@link Failure}.
     */
    T result();

    /**
     * Returns the error if this is a {@link Failure}, or {@code null} if this is a {@link Success}.
     */
    ProcessingError error();

    record Success<T>(T result) implements ActionResult<T> {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public ProcessingError error() {
            return null;
        }
    }

    record Failure<T>(ProcessingError error) implements ActionResult<T> {

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T result() {
            return null;
        }
    }
}
