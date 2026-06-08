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
package org.hyland.sdk.cic.nucleus.pagination;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Extracts the {@code cursor} query parameter from a Nucleus {@code next} URL.
 * <p>
 * The Nucleus API returns pagination links in the form {@code /users?cursor=<opaque>}. Only the opaque cursor value is
 * needed for the next request — passing the full URL would be incorrect.
 *
 * @since 1.0.0
 */
public final class NucleusCursorExtractor {

    private NucleusCursorExtractor() {
    }

    /**
     * Extracts the {@code cursor} query parameter value from a {@code next} URL.
     *
     * @param next the {@code next} value from a paginated response, or {@code null} when there are no more pages
     * @return the cursor value, or {@code null} if {@code next} is {@code null} or contains no {@code cursor} parameter
     */
    public static String extract(String next) {
        if (next == null) {
            return null;
        }
        int queryStart = next.indexOf('?');
        if (queryStart < 0) {
            return null;
        }
        String query = next.substring(queryStart + 1);
        for (String param : query.split("&")) {
            if (param.startsWith("cursor=")) {
                String encoded = param.substring("cursor=".length());
                return URLDecoder.decode(encoded, StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
