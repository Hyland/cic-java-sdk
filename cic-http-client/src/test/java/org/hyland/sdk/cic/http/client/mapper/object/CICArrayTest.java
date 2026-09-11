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
package org.hyland.sdk.cic.http.client.mapper.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
class CICArrayTest {

    @Test
    void testFromObjectArray() {
        assertEquals(List.of("a", "b"), CICArray.from(new String[] { "a", "b" }).toJavaValue());
    }

    @Test
    void testFromBooleanArray() {
        assertEquals(List.of(true, false), CICArray.from(new boolean[] { true, false }).toJavaValue());
    }

    @Test
    void testFromIntArray() {
        assertEquals(List.of(1, 2, 3), CICArray.from(new int[] { 1, 2, 3 }).toJavaValue());
    }

    @Test
    void testFromLongArray() {
        assertEquals(List.of(1L, 2L, 3L), CICArray.from(new long[] { 1L, 2L, 3L }).toJavaValue());
    }

    @Test
    void testFromDoubleArray() {
        assertEquals(List.of(1.5, 2.5), CICArray.from(new double[] { 1.5, 2.5 }).toJavaValue());
    }

    @Test
    void testFromCollection() {
        assertEquals(List.of("a", "b"), CICArray.from(List.of("a", "b")).toJavaValue());
    }

    @Test
    void testFromObjectArrayWithNestedPrimitiveArrays() {
        var values = new Object[] { new int[] { 1, 2 }, new boolean[] { true }, new long[] { 3L },
                new double[] { 4.5 } };
        assertEquals(List.of(List.of(1, 2), List.of(true), List.of(3L), List.of(4.5)),
                CICArray.from(values).toJavaValue());
    }

    @Test
    void testFromObjectArrayWithNestedObjectArray() {
        var values = new Object[] { new String[] { "a", "b" } };
        assertEquals(List.of(List.of("a", "b")), CICArray.from(values).toJavaValue());
    }

    @Test
    void testFromObjectArrayWithMap() {
        var values = new Object[] { Map.of("key", "value") };
        assertEquals(List.of(Map.of("key", "value")), CICArray.from(values).toJavaValue());
    }

    @Test
    void testFromObjectArrayWithUnsupportedArrayComponentTypeThrows() {
        var values = new Object[] { new char[] { 'a' } };
        assertThrows(CICSdkException.class, () -> CICArray.from(values));
    }

    @Test
    void testFromObjectArrayWithUnsupportedValueTypeThrows() {
        var values = new Object[] { new Object() };
        assertThrows(CICSdkException.class, () -> CICArray.from(values));
    }

    @Test
    void testEqualsAndHashCodeAreStructural() {
        var array1 = CICArray.from(new String[] { "a", "b" });
        var array2 = CICArray.from(new String[] { "a", "b" });
        var array3 = CICArray.from(new String[] { "a", "c" });

        assertEquals(array1, array2);
        assertEquals(array1.hashCode(), array2.hashCode());
        assertNotEquals(array1, array3);
    }

    @Test
    void testUnmodifiableThrowsOnMutation() {
        var array = CICArray.unmodifiable(CICArray.from(new String[] { "a" }));

        assertThrows(UnsupportedOperationException.class, () -> array.addString("b"));
        assertThrows(UnsupportedOperationException.class, () -> array.addBoolean(true));
        assertThrows(UnsupportedOperationException.class, () -> array.addInt(1));
        assertThrows(UnsupportedOperationException.class, () -> array.addLong(1L));
        assertThrows(UnsupportedOperationException.class, () -> array.addDouble(1.0));
        assertThrows(UnsupportedOperationException.class, () -> array.addArray(CICArray.create()));
        assertThrows(UnsupportedOperationException.class, () -> array.addObject(CICObject.create()));
    }

    @Test
    void testUnmodifiableStillReadableAndEqual() {
        var origin = CICArray.from(new String[] { "a", "b" });
        var array = CICArray.unmodifiable(origin);

        assertEquals(List.of("a", "b"), array.toJavaValue());
        assertEquals(origin, array);
        assertEquals(origin.hashCode(), array.hashCode());
    }

    @Test
    void testUnmodifiableNestedArrayAndObjectAreAlsoUnmodifiable() {
        var nestedArray = CICArray.create();
        nestedArray.addString("a");
        var nestedObject = CICObject.create();
        nestedObject.putString("key", "value");

        var origin = CICArray.create();
        origin.addArray(nestedArray);
        origin.addObject(nestedObject);

        var array = CICArray.unmodifiable(origin);

        var wrappedArray = array.getArray(0);
        assertThrows(UnsupportedOperationException.class, () -> wrappedArray.addString("b"));

        var wrappedObject = array.getObject(1);
        assertThrows(UnsupportedOperationException.class, () -> wrappedObject.putString("other", "value"));

        var elements = array.getElements();
        assertThrows(UnsupportedOperationException.class, () -> ((CICArray) elements.get(0)).addString("b"));
    }
}
