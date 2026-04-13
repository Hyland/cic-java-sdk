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
package org.hyland.sdk.cic.ingest.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class PropertyArrayTest {

    @Test
    void testEmptyIsSingleton() {
        assertSame(PropertyArray.empty(), PropertyArray.empty());
        assertTrue(PropertyArray.empty().elements().isEmpty());
    }

    @Test
    void testOfStrings() {
        var array = PropertyArray.of("a", "b", "c");
        assertEquals(List.of("a", "b", "c"), array.elements());
    }

    @Test
    void testOfInts() {
        var array = PropertyArray.of(1, 2, 3);
        assertEquals(List.of(1, 2, 3), array.elements());
    }

    @Test
    void testOfLongs() {
        var array = PropertyArray.of(1L, 2L, 3L);
        assertEquals(List.of(1L, 2L, 3L), array.elements());
    }

    @Test
    void testOfDoubles() {
        var array = PropertyArray.of(1.1, 2.2);
        assertEquals(List.of(1.1, 2.2), array.elements());
    }

    @Test
    void testOfBooleans() {
        var array = PropertyArray.of(true, false, true);
        assertEquals(List.of(true, false, true), array.elements());
    }

    @Test
    void testOfIngestEventProperties() {
        var props = IngestEventProperties.builder().put("id", "GROUP_EVERYONE").build();
        var array = PropertyArray.of(props);
        assertEquals(List.of(props), array.elements());
    }

    @Test
    void testNullStringElementThrows() {
        assertThrows(NullPointerException.class, () -> PropertyArray.of("a", null, "c"));
    }

    @Test
    void testNullIngestEventPropertiesElementThrows() {
        assertThrows(NullPointerException.class, () -> PropertyArray.of(IngestEventProperties.builder().build(), null));
    }

    @Test
    void testElementsIsUnmodifiable() {
        var array = PropertyArray.of("a", "b");
        assertThrows(UnsupportedOperationException.class, () -> array.elements().add("c"));
    }

    @Test
    void testEqualsSymmetry() {
        var a = PropertyArray.of("x", "y");
        var b = PropertyArray.of("x", "y");
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void testEqualsWithDifferentContent() {
        assertNotEquals(PropertyArray.of("x"), PropertyArray.of("y"));
    }

    @Test
    void testHashCodeConsistency() {
        assertEquals(PropertyArray.of("a", "b").hashCode(), PropertyArray.of("a", "b").hashCode());
    }

    @Test
    void testToString() {
        assertEquals(List.of("a", "b").toString(), PropertyArray.of("a", "b").toString());
    }
}
