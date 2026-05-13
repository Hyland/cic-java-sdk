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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class IngestEventTest {

    @Test
    void testNullObjectIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, null).sourceId("src1").build());
    }

    @Test
    void testBlankObjectIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "   ").sourceId("src1").build());
    }

    @Test
    void testNullPropertyKeyThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "obj1").putProperty(null, "value"));
    }

    @Test
    void testNullPropertyValueThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "obj1").putProperty("key", (Instant) null));
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "obj1").putProperty("key", (String) null));
        assertThrows(NullPointerException.class, () -> IngestEvent.builder(IngestEvent.Type.CREATE, "obj1")
                                                                  .putProperty("key", (IngestEventProperty) null));
    }

    @Test
    void testToBuilderPreservesAllFields() {
        var date = Instant.ofEpochMilli(1609459200000L);
        var original = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                                  .sourceId("src1")
                                  .date(date)
                                  .putProperty("key", "value")
                                  .build();

        var copy = original.toBuilder().build();

        assertEquals(original.type(), copy.type());
        assertEquals(original.sourceId(), copy.sourceId());
        assertEquals(original.objectId(), copy.objectId());
        assertEquals(original.date(), copy.date());
        assertEquals(original.properties(), copy.properties());
        assertNotSame(original, copy);
    }

    @Test
    void testToBuilderAllowsOverride() {
        var original = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").sourceId("src1").build();

        var modified = original.toBuilder().date(Instant.ofEpochMilli(9999999L)).putProperty("key2", "value2").build();

        assertEquals(IngestEvent.Type.CREATE, modified.type());
        assertEquals(Optional.of("src1"), modified.sourceId());
        assertEquals("doc1", modified.objectId());
        assertEquals(Instant.ofEpochMilli(9999999L), modified.date());
        assertEquals("value2", ((IngestEventPropertyValue) modified.properties().get("key2")).value().toJavaValue());
    }

    @Test
    void testPropertiesReplacesExisting() {
        var builder = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                                 .putProperty("key1", "value1")
                                 .putProperty("key2", "value2");

        var event = builder.properties(Map.of("key3", IngestEventPropertyValue.builder("value3").build())).build();

        assertEquals(1, event.properties().size());
        assertEquals("value3", ((IngestEventPropertyValue) event.properties().get("key3")).value().toJavaValue());
    }

    @Test
    void testPutPropertiesAddsToExisting() {
        var builder = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").putProperty("key1", "value1");

        var event = builder.putProperties(Map.of("key2", IngestEventPropertyValue.builder("value2").build())).build();

        assertEquals(2, event.properties().size());
        assertEquals("value1", ((IngestEventPropertyValue) event.properties().get("key1")).value().toJavaValue());
        assertEquals("value2", ((IngestEventPropertyValue) event.properties().get("key2")).value().toJavaValue());
    }

    @Test
    void testPropertiesNullThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").properties(null));
    }

    @Test
    void testPutPropertiesNullThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").putProperties(null));
    }

    @Test
    void testReplaceProperties() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                               .putProperty("key1", "value1")
                               .putProperty("key2", "value2")
                               .build();

        var replaced = event.toBuilder()
                            .replaceProperties(
                                    (key, property) -> IngestEventPropertyValue.builder("replaced-" + key).build())
                            .build();

        assertEquals("replaced-key1",
                ((IngestEventPropertyValue) replaced.properties().get("key1")).value().toJavaValue());
        assertEquals("replaced-key2",
                ((IngestEventPropertyValue) replaced.properties().get("key2")).value().toJavaValue());
    }
}
