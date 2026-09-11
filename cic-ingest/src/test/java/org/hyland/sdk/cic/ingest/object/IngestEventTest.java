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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * These tests build {@link IngestEvent} instances and then read them back for assertion purposes. In practice, reading
 * back an already-built event/properties is more involved than writing one, since the SDK's API is primarily designed
 * to let callers <em>build</em> and send events over the network, not to conveniently navigate/query them afterwards.
 *
 * @since 1.0.0
 */
class IngestEventTest {

    @Test
    void testNullObjectIdThrows() {
        assertThrows(NullPointerException.class,
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
        assertEquals("value2", modified.properties().getPropertyValue("key2"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPropertiesReplacesExisting() {
        var builder = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                                 .putProperty("key1", "value1")
                                 .putProperty("key2", "value2");

        var event = builder.properties(
                IngestEventProperties.builder().put("key3", IngestEventPropertyValue.builder("value3").build()).build())
                           .build();

        assertNull(event.properties().getProperty("key1"));
        assertNull(event.properties().getProperty("key2"));
        assertEquals("value3", event.properties().getPropertyValue("key3"));
    }

    @Test
    void testPutPropertyMap() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                               .putProperty("meta", Map.of("size", 1024L))
                               .build();
        assertEquals(Map.of("size", 1024L), event.properties().getPropertyValue("meta"));
    }

    @SuppressWarnings("removal")
    @Test
    void testPutPropertyIngestEventProperties() {
        var nested = IngestEventProperties.builder().put("size", 1024L).build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").putProperty("meta", nested).build();
        assertEquals(Map.of("size", 1024L), event.properties().getPropertyValue("meta"));
    }

    @Test
    void testPropertiesNullThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "doc1").properties(null));
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

        assertEquals("replaced-key1", replaced.properties().getPropertyValue("key1"));
        assertEquals("replaced-key2", replaced.properties().getPropertyValue("key2"));
    }

    @Test
    void testReplacePropertiesReturningNullRemovesProperty() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                               .putProperty("key1", "value1")
                               .putProperty("key2", "value2")
                               .build();

        var replaced = event.toBuilder()
                            .replaceProperties((key, property) -> "key1".equals(key) ? null : property)
                            .build();

        assertNull(replaced.properties().getProperty("key1"), "key1 should have been removed");
        assertNotNull(replaced.properties().getProperty("key2"), "key2 should be kept");
    }
}
