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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * @since 1.1.0
 */
public class CICBlobTest {

    @Test
    public void defaultMethodsReturnEmptyWhenNotImplemented() {
        var blob = new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }
        };
        assertTrue(blob.getContentType().isEmpty());
        assertTrue(blob.getName().isEmpty());
        assertTrue(blob.getSize().isEmpty());
    }

    @Test
    public void builderFromStringRequiresNonNullContent() {
        assertThrows(NullPointerException.class, () -> CICBlob.builder((String) null));
    }

    @Test
    public void builderFromBytesRequiresNonNullContent() {
        assertThrows(NullPointerException.class, () -> CICBlob.builder((byte[]) null));
    }

    @Test
    public void builderFromSupplierRequiresNonNullInputStreamSupplier() {
        assertThrows(NullPointerException.class, () -> CICBlob.builder((Supplier<InputStream>) null));
    }

    @Test
    public void builderRejectsNegativeSize() {
        assertThrows(IllegalArgumentException.class, () -> CICBlob.builder("test content").size(-1L));
    }

    @Test
    public void builderRejectsNullInputStreamFromSupplier() {
        var blob = CICBlob.builder((Supplier<InputStream>) () -> null).build();

        assertThrows(NullPointerException.class, blob::getInputStream);
    }

    @Test
    public void builderFromBytesDoesNotShareContentWithCallerArray() throws IOException {
        var content = "test content".getBytes(StandardCharsets.UTF_8);
        var blob = CICBlob.builder(content).build();
        content[0] = 'X';

        try (var is = blob.getInputStream()) {
            assertEquals("test content", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void builderBuildsBlobFromString() throws IOException {
        var blob = CICBlob.builder("test content")
                          .contentType("text/plain")
                          .name("file.txt")
                          .size(12L)
                          .digest("sha256:abc123")
                          .build();

        assertEquals("text/plain", blob.getContentType().orElseThrow());
        assertEquals("file.txt", blob.getName().orElseThrow());
        assertEquals(12L, blob.getSize().orElseThrow());
        assertEquals("sha256:abc123", blob.getDigest().orElseThrow());
        try (var is = blob.getInputStream()) {
            assertEquals("test content", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void builderBuildsBlobFromBytes() throws IOException {
        var content = "test content".getBytes(StandardCharsets.UTF_8);
        var blob = CICBlob.builder(content).contentType("application/octet-stream").digest("sha256:abc123").build();

        assertEquals("application/octet-stream", blob.getContentType().orElseThrow());
        assertEquals("sha256:abc123", blob.getDigest().orElseThrow());
        try (var is = blob.getInputStream()) {
            assertEquals("test content", new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void builderBuildsBlobFromSupplierWithAllAttributes() throws IOException {
        var content = "test content";
        var blob = CICBlob.builder(() -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
                          .contentType("application/pdf")
                          .name("file.pdf")
                          .size(42L)
                          .digest("sha256:abc123")
                          .build();

        assertEquals("application/pdf", blob.getContentType().orElseThrow());
        assertEquals("file.pdf", blob.getName().orElseThrow());
        assertEquals(42L, blob.getSize().orElseThrow());
        assertEquals("sha256:abc123", blob.getDigest().orElseThrow());
        try (var is = blob.getInputStream()) {
            assertEquals(content, new String(is.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void builderBuildsBlobWithoutOptionalAttributes() {
        var blob = CICBlob.builder(new byte[0]).build();

        assertTrue(blob.getContentType().isEmpty());
        assertTrue(blob.getName().isEmpty());
        assertTrue(blob.getSize().isEmpty());
        assertTrue(blob.getDigest().isEmpty());
    }

    @Test
    public void builderFromBytesReturnsNewInputStreamOnEachCall() throws IOException {
        var blob = CICBlob.builder("test content".getBytes(StandardCharsets.UTF_8)).build();

        InputStream first = blob.getInputStream();
        InputStream second = blob.getInputStream();
        assertNotSame(first, second, "Each call to getInputStream() should return a new InputStream");
        try (first; second) {
            assertEquals("test content", new String(first.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("test content", new String(second.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void builderFromSupplierInputStreamSupplierIsCalledOnEachGetInputStream() throws IOException {
        var content = "test content".getBytes(StandardCharsets.UTF_8);
        var blob = CICBlob.builder(() -> new ByteArrayInputStream(content)).build();

        InputStream first = blob.getInputStream();
        InputStream second = blob.getInputStream();
        assertNotSame(first, second, "Each call to getInputStream() should return a new InputStream");
        try (first; second) {
            assertEquals("test content", new String(first.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("test content", new String(second.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
