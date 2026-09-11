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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
class CICNodeTest {

    @Test
    void testFromNull() {
        assertNull(CICNode.from(null).toJavaValue());
    }

    @Test
    void testFromBoolean() {
        assertEquals(true, CICNode.from(true).toJavaValue());
    }

    @Test
    void testFromInteger() {
        assertEquals(1, CICNode.from(1).toJavaValue());
    }

    @Test
    void testFromLong() {
        assertEquals(1L, CICNode.from(1L).toJavaValue());
    }

    @Test
    void testFromDouble() {
        assertEquals(1.5, CICNode.from(1.5).toJavaValue());
    }

    @Test
    void testFromString() {
        assertEquals("value", CICNode.from("value").toJavaValue());
    }

    @Test
    void testFromObjectArray() {
        Object value = new String[] { "a", "b" };
        assertEquals(List.of("a", "b"), CICNode.from(value).toJavaValue());
    }

    @Test
    void testFromBooleanArray() {
        Object value = new boolean[] { true, false };
        assertEquals(List.of(true, false), CICNode.from(value).toJavaValue());
    }

    @Test
    void testFromIntArray() {
        Object value = new int[] { 1, 2, 3 };
        assertEquals(List.of(1, 2, 3), CICNode.from(value).toJavaValue());
    }

    @Test
    void testFromLongArray() {
        Object value = new long[] { 1L, 2L, 3L };
        assertEquals(List.of(1L, 2L, 3L), CICNode.from(value).toJavaValue());
    }

    @Test
    void testFromDoubleArray() {
        Object value = new double[] { 1.5, 2.5 };
        assertEquals(List.of(1.5, 2.5), CICNode.from(value).toJavaValue());
    }

    @Test
    void testFromUnsupportedArrayComponentTypeThrows() {
        Object value = new char[] { 'a', 'b' };
        assertThrows(CICSdkException.class, () -> CICNode.from(value));
    }

    @Test
    void testFromCollection() {
        assertEquals(List.of("a", "b"), CICNode.from(List.of("a", "b")).toJavaValue());
    }

    @Test
    void testFromMap() {
        assertEquals(Map.of("key", "value"), CICNode.from(Map.of("key", "value")).toJavaValue());
    }

    @Test
    void testFromUnsupportedTypeThrows() {
        assertThrows(CICSdkException.class, () -> CICNode.from(new Object()));
    }

    @Test
    void testUnmodifiableWrapsArray() {
        var node = CICNode.unmodifiable(CICArray.from(new String[] { "a" }));
        assertThrows(UnsupportedOperationException.class, () -> ((CICArray) node).addString("b"));
    }

    @Test
    void testUnmodifiableWrapsObject() {
        var node = CICNode.unmodifiable(CICObject.from(Map.of("key", "value")));
        assertThrows(UnsupportedOperationException.class, () -> ((CICObject) node).putString("other", "value"));
    }

    @Test
    void testUnmodifiableReturnsPrimitiveAsIs() {
        var node = CICNode.from("value");
        assertEquals(node, CICNode.unmodifiable(node));
    }

    @Test
    void testUnmodifiableReturnsNullAsIs() {
        assertNull(CICNode.unmodifiable(CICNode.from(null)).toJavaValue());
    }
}
