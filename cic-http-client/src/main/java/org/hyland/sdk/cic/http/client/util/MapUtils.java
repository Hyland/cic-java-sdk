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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.http.client.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.0.0
 */
public final class MapUtils {

    private MapUtils() {
        // utility class
    }

    public static <K, V> Map<K, List<V>> copyOfMultivalued(Map<K, List<V>> map) {
        var copy = new LinkedHashMap<>(map);
        copy.replaceAll((key, value) -> List.copyOf(value));
        return Collections.unmodifiableMap(copy);
    }
}
