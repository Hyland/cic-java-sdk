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

import java.util.Map;
import java.util.Objects;

/**
 * Represents an arbitrary JSON object expression passed to or received from the QnA API, such as a filter expression.
 *
 * @since 1.0.0
 */
public record FilterExpression(Map<String, Object> properties) {

    public FilterExpression {
        Objects.requireNonNull(properties, "properties cannot be null");
        properties = Map.copyOf(properties);
    }

    public static FilterExpression of(Map<String, Object> properties) {
        return new FilterExpression(properties);
    }
}
