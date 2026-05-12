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
package org.hyland.sdk.cic.qna;

import java.util.Objects;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.qna.object.Answer;

/**
 * Service for integration-specific QnA operations.
 *
 * @since 1.0.0
 */
public class IntegrationQnaService {

    protected final QnaHttpClient httpClient;

    IntegrationQnaService(QnaHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Returns an {@link IntegrationQuestionResource} handle bound to the given question ID.
     *
     * @param questionId the question ID
     * @return the integration question resource handle
     * @throws NullPointerException if questionId is null
     */
    public IntegrationQuestionResource question(String questionId) {
        return new IntegrationQuestionResource(httpClient,
                Objects.requireNonNull(questionId, "questionId cannot be null"));
    }

    /**
     * A resource handle bound to a specific question ID for integration operations.
     *
     * @since 1.0.0
     */
    public static class IntegrationQuestionResource {

        private final QnaHttpClient httpClient;

        private final String questionId;

        private IntegrationQuestionResource(QnaHttpClient httpClient, String questionId) {
            this.httpClient = httpClient;
            this.questionId = questionId;
        }

        /**
         * Returns the question ID this handle is bound to.
         *
         * @return the question ID
         */
        public String id() {
            return questionId;
        }

        /**
         * Gets the answer for this question via the integrations endpoint.
         *
         * @param userId optional user ID (may be null)
         * @return the answer
         * @throws CICSdkException if the request fails
         */
        public Answer getAnswer(String userId) {
            return httpClient.getIntegrationQuestionAnswer(questionId, userId);
        }
    }
}
