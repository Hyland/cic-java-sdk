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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class NucleusCursorExtractorTest {

    @Test
    void testExtractSimpleCursor() {
        assertEquals("abc123", NucleusCursorExtractor.extract("/users?cursor=abc123"));
    }

    @Test
    void testExtractCursorWithOtherParams() {
        assertEquals("abc123", NucleusCursorExtractor.extract("/users?limit=10&cursor=abc123&filter=active"));
    }

    @Test
    void testExtractCursorUrlDecoded() {
        assertEquals("abc/def==", NucleusCursorExtractor.extract("/users?cursor=abc%2Fdef%3D%3D&filter=active"));
    }

    @Test
    void testExtractNullReturnsNull() {
        assertNull(NucleusCursorExtractor.extract(null));
    }

    @Test
    void testExtractNoQueryStringReturnsNull() {
        assertNull(NucleusCursorExtractor.extract("/users"));
    }

    @Test
    void testExtractNoCursorParamReturnsNull() {
        assertNull(NucleusCursorExtractor.extract("/users?limit=10&filter=active"));
    }

    @Test
    void testExtractAbsoluteUrl() {
        assertEquals("abc123", NucleusCursorExtractor.extract("https://localhost:8080/api/users?cursor=abc123"));
    }
}
