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
package org.hyland.sdk.cic.agent.object;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents an arbitrary JSON object expression passed to or received from the Agent API, such as a static filter
 * expression, dynamic filter template, or dynamic filter.
 * <p>
 * Property values may be any of the following types:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Integer}</li>
 * <li>{@link Long}</li>
 * <li>{@link Double}</li>
 * <li>{@link Boolean}</li>
 * <li>{@code null}</li>
 * <li>{@link java.util.List List&lt;Object&gt;} — for JSON arrays (elements must follow the same rules, except that
 * {@code null} elements are not supported)</li>
 * <li>{@link Map Map&lt;String, Object&gt;} — for nested JSON objects (values follow the same rules)</li>
 * </ul>
 *
 * @since 1.0.0
 */
public record FilterExpression(Map<String, Object> properties) {

    public FilterExpression {
        Objects.requireNonNull(properties, "properties cannot be null");
        var copy = new LinkedHashMap<>(properties);
        if (copy.containsKey(null)) {
            throw new NullPointerException("property key cannot be null");
        }
        properties = Collections.unmodifiableMap(copy);
    }

    /**
     * Creates a {@link FilterExpression} from the given property map.
     *
     * @param properties the filter expression properties; values must conform to the allowed types
     * @return the filter expression
     */
    public static FilterExpression of(Map<String, Object> properties) {
        return new FilterExpression(properties);
    }
}
