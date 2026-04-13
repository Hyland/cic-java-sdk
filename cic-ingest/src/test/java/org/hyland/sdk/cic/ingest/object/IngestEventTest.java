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

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class IngestEventTest {

    @Test
    void testNullSourceIdThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, null, "doc1").build());
    }

    @Test
    void testBlankSourceIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "  ", "doc1").build());
    }

    @Test
    void testNullObjectIdThrows() {
        assertThrows(NullPointerException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "src1", null).build());
    }

    @Test
    void testBlankObjectIdThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> IngestEvent.builder(IngestEvent.Type.CREATE, "src1", "  ").build());
    }

    @Test
    void testToBuilderPreservesAllFields() {
        var date = Instant.ofEpochMilli(1609459200000L);
        var props = IngestEventProperties.builder().put("key", "value").build();
        var original = IngestEvent.builder(IngestEvent.Type.CREATE, "src1", "doc1")
                                  .date(date)
                                  .properties(props)
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
        var original = IngestEvent.builder(IngestEvent.Type.CREATE, "src1", "doc1").build();

        var modified = original.toBuilder().date(Instant.ofEpochMilli(9999999L)).putProperty("extra", "val").build();

        assertEquals(IngestEvent.Type.CREATE, modified.type());
        assertEquals("src1", modified.sourceId());
        assertEquals("doc1", modified.objectId());
        assertEquals(Instant.ofEpochMilli(9999999L), modified.date());
        assertEquals("val", modified.properties().toMap().get("extra"));
    }
}
