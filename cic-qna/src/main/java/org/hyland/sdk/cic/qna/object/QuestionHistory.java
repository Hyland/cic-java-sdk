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
package org.hyland.sdk.cic.qna.object;

import java.util.Objects;

/**
 * @param dateCreated the creation timestamp as an ISO-8601 string (e.g. {@code "2026-04-02T11:42:00Z"}), may be
 *            {@code null}
 * @param dateAnswered the answered timestamp as an ISO-8601 string, may be {@code null}
 * @since 1.0.0
 */
public record QuestionHistory(String id, String question, String answer, String dateCreated, String dateAnswered,
        int agentVersion, ResponseCompleteness responseCompleteness, FeedbackType feedback,
        FilterExpression staticFilter, FilterExpression dynamicFilter) {

    public QuestionHistory {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(question, "question cannot be null");
    }
}
