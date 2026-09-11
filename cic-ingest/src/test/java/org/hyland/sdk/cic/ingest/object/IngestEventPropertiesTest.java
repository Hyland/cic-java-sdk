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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class IngestEventPropertiesTest {

    @Test
    void testPutString() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertEquals("value", props.getPropertyValue("key"));
    }

    @Test
    void testPutStringArray() {
        var props = IngestEventProperties.builder().put("tags", new String[] { "a", "b" }).build();
        assertEquals(List.of("a", "b"), props.getPropertyValue("tags"));
    }

    @Test
    void testPutInt() {
        var props = IngestEventProperties.builder().put("count", 42).build();
        assertEquals(42, props.getPropertyValue("count"));
    }

    @Test
    void testPutLong() {
        var props = IngestEventProperties.builder().put("timestamp", 1609459200000L).build();
        assertEquals(1609459200000L, props.getPropertyValue("timestamp"));
    }

    @Test
    void testPutDouble() {
        var props = IngestEventProperties.builder().put("score", 9.5).build();
        assertEquals(9.5, props.getPropertyValue("score"));
    }

    @Test
    void testPutBoolean() {
        var props = IngestEventProperties.builder().put("active", true).build();
        assertEquals(true, props.getPropertyValue("active"));
    }

    @Test
    void testPutInstant() {
        var date = Instant.ofEpochMilli(1609459200000L);
        var props = IngestEventProperties.builder().put("date", date).build();
        assertEquals(date.toString(), props.getPropertyValue("date"));
    }

    @Test
    void testPutNestedMap() {
        var props = IngestEventProperties.builder()
                                         .put("meta",
                                                 IngestEventPropertyValue.builder(Map.of("size", 1024L, "type", "pdf"))
                                                                         .build())
                                         .build();
        assertEquals(Map.of("size", 1024L, "type", "pdf"), props.getPropertyValue("meta"));
    }

    @Test
    void testPutMap() {
        var props = IngestEventProperties.builder().put("meta", Map.of("size", 1024L)).build();
        assertEquals(Map.of("size", 1024L), props.getPropertyValue("meta"));
    }

    @Test
    void testPutMapArray() {
        var props = IngestEventProperties.builder()
                                         .put("meta", new Map[] { Map.of("size", 1024L), Map.of("type", "pdf") })
                                         .build();
        assertEquals(List.of(Map.of("size", 1024L), Map.of("type", "pdf")), props.getPropertyValue("meta"));
    }

    @Test
    void testPutIngestEventPropertyFile() {
        var file = IngestEventPropertyFile.builder().id("id1").build();
        var props = IngestEventProperties.builder().put("file:content", file).build();
        assertEquals(file, props.getProperty("file:content"));
    }

    @Test
    void testGetPropertyValueThrowsWhenNotAValue() {
        var file = IngestEventPropertyFile.builder().id("id1").build();
        var props = IngestEventProperties.builder().put("file:content", file).build();
        assertThrows(IllegalStateException.class, () -> props.getPropertyValue("file:content"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutIngestEventProperties() {
        var nested = IngestEventProperties.builder().put("size", 1024L).put("name", "doc.pdf").build();
        var props = IngestEventProperties.builder().put("nested", nested).build();
        IngestEventPropertyValue property = props.getProperty("nested");
        assertEquals(IngestEventPropertyValue.Type.UNKNOWN, property.type());
        assertEquals(Map.of("size", 1024L, "name", "doc.pdf"), props.getPropertyValue("nested"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutPropertyArray() {
        var array = PropertyArray.of("a", "b");
        var props = IngestEventProperties.builder().put("tags", array).build();
        assertEquals(List.of("a", "b"), props.getPropertyValue("tags"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutConsumer() {
        // A property built via a Consumer<Builder> is UNKNOWN-typed and flattened the same way as top-level ones,
        // so no literal "type":"unknown" is ever sent over the wire.
        var props = IngestEventProperties.builder().put("meta", b -> b.put("size", 1024L).put("type", "pdf")).build();
        assertEquals(Map.of("size", 1024L, "type", "pdf"), props.getPropertyValue("meta"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutNestedProperties() {
        // A nested IngestEventProperties is flattened the same way regardless of nesting depth (see also
        // testPutIngestEventProperties, which asserts the resulting Type.UNKNOWN wrapper too).
        var nested = IngestEventProperties.builder().put("inner", "val").build();
        var props = IngestEventProperties.builder().put("meta", nested).build();
        assertEquals(Map.of("inner", "val"), props.getPropertyValue("meta"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutConsumerWithNestedFileThrows() {
        // IngestEventPropertyFile didn't exist prior to 1.1.0, so there's no backward compatibility to preserve for
        // nesting a file property within this deprecated idiom.
        var file = IngestEventPropertyFile.builder().id("id1").build();
        var builder = IngestEventProperties.builder();
        assertThrows(IllegalArgumentException.class,
                () -> builder.put("nested", nestedBuilder -> nestedBuilder.put("file", file)));
    }

    @Test
    void testPutNull() {
        var props = IngestEventProperties.builder().putNull("key").build();
        assertNull(props.getPropertyValue("key"));
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
    void testNullPropertyThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEventProperties.builder().put("key", (IngestEventProperty) null));
    }

    @SuppressWarnings("removal")
    @Test
    void testNullPropertyArrayThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEventProperties.builder().put("key", (PropertyArray) null));
    }

    @SuppressWarnings("removal")
    @Test
    void testNullPropertiesValueThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEventProperties.builder().put("key", (IngestEventProperties) null));
    }

    @Test
    void testGetPropertyMissingKeyReturnsNull() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertNull(props.getPropertyValue("missing"));
    }

    @Test
    void testIsEmpty() {
        assertTrue(IngestEventProperties.builder().build().isEmpty());
    }

    @Test
    void testEmptyProperties() {
        var props = IngestEventProperties.builder().build();
        assertTrue(props.isEmpty());
    }

    @SuppressWarnings("removal")
    @Test
    void testToMapIsUnmodifiable() {
        var props = IngestEventProperties.builder().put("key", "value").build();
        assertThrows(UnsupportedOperationException.class, () -> props.toMap().put("other", "val"));
    }

    @SuppressWarnings("removal")
    @Test
    void testToMapReturnsRawJavaValuesForBackwardCompatibility() {
        var props = IngestEventProperties.builder().put("title", "value").put("count", 42).putNull("nothing").build();

        var map = props.toMap();

        // a pre-1.1.0 caller casting to the original Java type must keep working
        assertEquals("value", map.get("title"));
        assertEquals(42, map.get("count"));
        assertNull(map.get("nothing"));
    }

    @SuppressWarnings("removal")
    @Test
    void testToMapReconstructsPropertyArrayForBackwardCompatibility() {
        var props = IngestEventProperties.builder().put("tags", PropertyArray.of("a", "b")).build();

        // a pre-1.1.0 caller casting toMap().get(key) to PropertyArray must keep working
        assertEquals(PropertyArray.of("a", "b"), props.toMap().get("tags"));
    }

    @SuppressWarnings("removal")
    @Test
    void testToMapFallsBackToListForUnsupportedLegacyArrayShapes() {
        // nested IngestEventProperties elements have no PropertyArray-compatible reconstruction: falls back to the
        // raw unwrapped value, same as any other array built via the new typed API
        var nested = IngestEventProperties.builder().put("key", "value").build();
        var props = IngestEventProperties.builder().put("items", PropertyArray.of(nested)).build();

        assertEquals(List.of(Map.of("key", "value")), props.toMap().get("items"));
    }

    @Test
    void testBuildSnapshotIsolation() {
        var builder = IngestEventProperties.builder().put("key", "first");
        var first = builder.build();
        builder.put("key", "second");
        var second = builder.build();

        assertEquals("first", first.getPropertyValue("key"));
        assertEquals("second", second.getPropertyValue("key"));
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
        assertEquals(Map.of("key", IngestEventPropertyValue.builder("value").build()).toString(), props.toString());
    }
}
