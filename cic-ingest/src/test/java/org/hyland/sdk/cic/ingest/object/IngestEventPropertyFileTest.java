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
package org.hyland.sdk.cic.ingest.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;

/**
 * @since 1.0.0
 */
class IngestEventPropertyFileTest {

    @Test
    public void testBuildEmpty() {
        var file = IngestEventPropertyFile.builder().build();

        assertTrue(file.blob().isEmpty());
        assertTrue(file.id().isEmpty());
        assertTrue(file.contentType().isEmpty());
        assertTrue(file.name().isEmpty());
        assertTrue(file.size().isEmpty());
        assertTrue(file.digest().isEmpty());
    }

    @Test
    public void testBuildWithAllFields() {
        var file = IngestEventPropertyFile.builder()
                                          .id("file-id")
                                          .contentType("application/pdf")
                                          .name("doc.pdf")
                                          .size(42L)
                                          .digest("sha256:abc")
                                          .build();

        assertEquals(Optional.of("file-id"), file.id());
        assertEquals(Optional.of("application/pdf"), file.contentType());
        assertEquals(Optional.of("doc.pdf"), file.name());
        assertEquals(OptionalLong.of(42L), file.size());
        assertEquals(Optional.of("sha256:abc"), file.digest());
    }

    @Test
    public void testBuildBlobWithoutContentTypeThrows() {
        var blob = CICBlob.builder(new ByteArrayInputStream(new byte[0])).build();

        assertThrows(IllegalArgumentException.class, () -> IngestEventPropertyFile.builder(blob).build());
    }

    @Test
    public void testBuildPartialMetadataThrows() {
        // size set, name and contentType missing
        assertThrows(IllegalArgumentException.class, () -> IngestEventPropertyFile.builder().size(10L).build());
        // name set, size and contentType missing
        assertThrows(IllegalArgumentException.class, () -> IngestEventPropertyFile.builder().name("doc.pdf").build());
        // size and name set, contentType missing
        assertThrows(IllegalArgumentException.class,
                () -> IngestEventPropertyFile.builder().size(10L).name("doc.pdf").build());
    }

    @Test
    public void testBuilderFromBlobInheritsMetadata() {
        var blob = CICBlob.builder(new ByteArrayInputStream(new byte[0]))
                          .contentType("application/pdf")
                          .name("doc.pdf")
                          .size(42L)
                          .digest("sha256:abc")
                          .build();

        var file = IngestEventPropertyFile.builder(blob).build();

        assertTrue(file.blob().isPresent());
        assertEquals(Optional.of("application/pdf"), file.contentType());
        assertEquals(Optional.of("doc.pdf"), file.name());
        assertEquals(OptionalLong.of(42L), file.size());
        assertEquals(Optional.of("sha256:abc"), file.digest());
    }

    @Test
    public void testBuilderFromNullBlobThrows() {
        assertThrows(NullPointerException.class, () -> IngestEventPropertyFile.builder(null));
    }

    @Test
    public void testToBuilderDropsBlob() {
        var blob = CICBlob.builder(new ByteArrayInputStream(new byte[0]))
                          .contentType("application/pdf")
                          .name("doc.pdf")
                          .size(42L)
                          .digest("sha256:abc")
                          .build();
        var file = IngestEventPropertyFile.builder(blob).id("uploaded-id").build();

        var copy = file.toBuilder().build();

        assertTrue(copy.blob().isEmpty());
        assertEquals(file.id(), copy.id());
        assertEquals(file.contentType(), copy.contentType());
        assertEquals(file.name(), copy.name());
        assertEquals(file.size(), copy.size());
        assertEquals(file.digest(), copy.digest());
    }

    @Test
    public void testEqualsAndHashCodeBasedOnId() {
        var file1 = IngestEventPropertyFile.builder().id("same").contentType("application/pdf").build();
        var file2 = IngestEventPropertyFile.builder().id("same").contentType("text/plain").build();
        var file3 = IngestEventPropertyFile.builder().id("other").contentType("application/pdf").build();

        assertEquals(file1, file2);
        assertEquals(file1.hashCode(), file2.hashCode());
        assertNotEquals(file1, file3);
        assertFalse(file1.equals("not a file"));
    }

    @Test
    public void testToString() {
        var file = IngestEventPropertyFile.builder().id("file-id").contentType("application/pdf").build();
        assertEquals("{id=file-id,contentType=application/pdf}", file.toString());
    }
}
