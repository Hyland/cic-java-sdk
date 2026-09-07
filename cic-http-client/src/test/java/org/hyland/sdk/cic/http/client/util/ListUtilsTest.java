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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class ListUtilsTest {

    @Test
    public void testEmpty() {
        assertEquals(List.of(), ListUtils.asList());
    }

    @Test
    public void testBoolean() {
        assertEquals(List.of(true), ListUtils.asList(true));
        assertEquals(List.of(true, false, true), ListUtils.asList(true, false, true));
        assertEquals(List.of(true, false, true), ListUtils.asList(new boolean[] { true, false, true }));
        assertEquals(List.of(true, false, true), ListUtils.asList(true, new boolean[] { false, true }));
    }

    @Test
    public void testInt() {
        assertEquals(List.of(1), ListUtils.asList(1));
        assertEquals(List.of(1, 2, 3), ListUtils.asList(1, 2, 3));
        assertEquals(List.of(1, 2, 3), ListUtils.asList(new int[] { 1, 2, 3 }));
        assertEquals(List.of(1, 2, 3), ListUtils.asList(1, new int[] { 2, 3 }));
    }

    @Test
    public void testLong() {
        assertEquals(List.of(1L), ListUtils.asList(1L));
        assertEquals(List.of(1L, 2L, 3L), ListUtils.asList(1L, 2L, 3L));
        assertEquals(List.of(1L, 2L, 3L), ListUtils.asList(new long[] { 1L, 2L, 3L }));
        assertEquals(List.of(1L, 2L, 3L), ListUtils.asList(1L, new long[] { 2L, 3L }));
    }

    @Test
    public void testDouble() {
        assertEquals(List.of(1.5), ListUtils.asList(1.5));
        assertEquals(List.of(1.5, 2.5, 3.5), ListUtils.asList(1.5, 2.5, 3.5));
        assertEquals(List.of(1.5, 2.5, 3.5), ListUtils.asList(new double[] { 1.5, 2.5, 3.5 }));
        assertEquals(List.of(1.5, 2.5, 3.5), ListUtils.asList(1.5, new double[] { 2.5, 3.5 }));
    }

    @Test
    public void testString() {
        assertEquals(List.of("a"), ListUtils.asList("a"));
        assertEquals(List.of("a", "b", "c"), ListUtils.asList("a", "b", "c"));
        assertEquals(List.of("a", "b", "c"), ListUtils.asList(new String[] { "a", "b", "c" }));
        assertEquals(List.of("a", "b", "c"), ListUtils.asList("a", new String[] { "b", "c" }));
    }

    @Test
    public void testInstant() {
        var i1 = Instant.parse("2025-01-01T00:00:00Z");
        var i2 = Instant.parse("2025-01-02T00:00:00Z");
        var i3 = Instant.parse("2025-01-03T00:00:00Z");
        assertEquals(List.of(i1), ListUtils.asList(i1));
        assertEquals(List.of(i1, i2, i3), ListUtils.asList(i1, i2, i3));
        assertEquals(List.of(i1, i2, i3), ListUtils.asList(new Instant[] { i1, i2, i3 }));
        assertEquals(List.of(i1, i2, i3), ListUtils.asList(i1, new Instant[] { i2, i3 }));
    }
}
