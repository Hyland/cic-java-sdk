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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
public class CICBlobTest {

    @Test
    public void testBuilderRequiresInputStream() {
        assertThrows(NullPointerException.class, () -> CICBlob.builder(null));
    }

    @Test
    public void testBuilderMinimal() {
        InputStream is = new ByteArrayInputStream("hello".getBytes());
        var blob = CICBlob.builder(is).build();
        assertNotNull(blob);
        assertSame(is, blob.getInputStream());
        assertEquals(Optional.empty(), blob.getContentType());
        assertEquals(Optional.empty(), blob.getName());
        assertEquals(OptionalLong.empty(), blob.getSize());
        assertEquals(Optional.empty(), blob.getDigest());
    }

    @Test
    public void testBuilderWithAllFields() {
        InputStream is = new ByteArrayInputStream("hello".getBytes());
        var blob = CICBlob.builder(is)
                          .contentType("text/markdown")
                          .name("readme.md")
                          .size(42L)
                          .digest("sha256:abc")
                          .build();
        assertSame(is, blob.getInputStream());
        assertEquals(Optional.of("text/markdown"), blob.getContentType());
        assertEquals(Optional.of("readme.md"), blob.getName());
        assertEquals(OptionalLong.of(42L), blob.getSize());
        assertEquals(Optional.of("sha256:abc"), blob.getDigest());
    }

    @Test
    public void testBuilderMutationAfterBuildDoesNotAffectInstance() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        var builder = CICBlob.builder(is).digest("sha256:abc");
        var blob = builder.build();
        builder.digest("sha256:zzz");
        assertEquals(Optional.of("sha256:abc"), blob.getDigest());
    }
}
