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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class IngestEventPropertiesTest {

    @Test
    void testPutString() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertEquals("value", props.toMap().get("key"));
    }

    @Test
    void testPutInt() {
        var props = IngestEventProperties.builder().put("count", 42).build();
        assertEquals(42, props.toMap().get("count"));
    }

    @Test
    void testPutLong() {
        var props = IngestEventProperties.builder().put("timestamp", 1609459200000L).build();
        assertEquals(1609459200000L, props.toMap().get("timestamp"));
    }

    @Test
    void testPutDouble() {
        var props = IngestEventProperties.builder().put("score", 9.5).build();
        assertEquals(9.5, props.toMap().get("score"));
    }

    @Test
    void testPutBoolean() {
        var props = IngestEventProperties.builder().put("active", true).build();
        assertEquals(true, props.toMap().get("active"));
    }

    @Test
    void testPutPropertyArray() {
        var array = PropertyArray.of("a", "b");
        var props = IngestEventProperties.builder().put("tags", array).build();
        assertEquals(array, props.toMap().get("tags"));
    }

    @Test
    void testPutPropertyArrayViaConsumer() {
        var props = IngestEventProperties.builder().put("meta", b -> b.put("size", 1024L).put("type", "pdf")).build();
        var meta = (IngestEventProperties) props.toMap().get("meta");
        assertEquals(1024L, meta.toMap().get("size"));
        assertEquals("pdf", meta.toMap().get("type"));
    }

    @Test
    void testPutNestedProperties() {
        var nested = IngestEventProperties.builder().put("inner", "val").build();
        var props = IngestEventProperties.builder().put("meta", nested).build();
        assertEquals(nested, props.toMap().get("meta"));
    }

    @Test
    void testNullKeyThrows() {
        assertThrows(NullPointerException.class, () -> IngestEventProperties.builder().put(null, "value"));
    }

    @Test
    void testNullStringValueThrows() {
        assertThrows(NullPointerException.class, () -> IngestEventProperties.builder().put("key", (String) null));
    }

    @Test
    void testNullPropertyArrayThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEventProperties.builder().put("key", (PropertyArray) null));
    }

    @Test
    void testNullPropertiesValueThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEventProperties.builder().put("key", (IngestEventProperties) null));
    }

    @Test
    void testIsEmpty() {
        assertTrue(IngestEventProperties.builder().build().isEmpty());
    }

    @Test
    void testEmptyProperties() {
        var props = IngestEventProperties.builder().build();
        assertTrue(props.toMap().isEmpty());
    }

    @Test
    void testToMapIsUnmodifiable() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertThrows(UnsupportedOperationException.class, () -> props.toMap().put("other", "val"));
    }

    @Test
    void testBuildSnapshotIsolation() {
        var builder = IngestEventProperties.builder().put("key", "first");
        var first = builder.build();
        builder.put("key", "second");
        var second = builder.build();

        assertEquals("first", first.toMap().get("key"));
        assertEquals("second", second.toMap().get("key"));
        assertNotSame(first, second);
    }

    @Test
    void testEqualsSymmetry() {
        var a = IngestEventProperties.builder().put("k", "v").build();
        var b = IngestEventProperties.builder().put("k", "v").build();
        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    void testEqualsWithDifferentContent() {
        var a = IngestEventProperties.builder().put("k", "v1").build();
        var b = IngestEventProperties.builder().put("k", "v2").build();
        assertNotEquals(a, b);
    }

    @Test
    void testHashCodeConsistency() {
        var a = IngestEventProperties.builder().put("k", "v").build();
        var b = IngestEventProperties.builder().put("k", "v").build();
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testToString() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertEquals(Map.of("key", "value").toString(), props.toString());
    }
}
