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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

/**
 * @since 1.0.0
 */
public interface CICBlob extends CICMarker {

    /**
     * Returns a new {@link InputStream} to read the blob's content.
     * <p>
     * Each call to this method is expected to return a fresh, unconsumed stream so that the blob's content can be read
     * multiple times, for instance when an upload needs to be retried.
     */
    InputStream getInputStream();

    Optional<String> getDigest();

    default Optional<String> getContentType() {
        return Optional.empty();
    }

    /**
     * @since 1.1.0
     */
    default Optional<String> getName() {
        return Optional.empty();
    }

    /**
     * @since 1.1.0
     */
    default OptionalLong getSize() {
        return OptionalLong.empty();
    }

    /**
     * Creates a {@link Builder} for a {@link CICBlob} whose content is the given {@code content} string, encoded as
     * UTF-8.
     *
     * @since 1.1.0
     */
    static Builder builder(String content) {
        Objects.requireNonNull(content, "content cannot be null");
        return builder(() -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Creates a {@link Builder} for a {@link CICBlob} whose content is the given {@code content} bytes.
     *
     * @since 1.1.0
     */
    static Builder builder(byte[] content) {
        Objects.requireNonNull(content, "content cannot be null");
        var contentCopy = content.clone();
        return builder(() -> new ByteArrayInputStream(contentCopy));
    }

    /**
     * Creates a {@link Builder} for a {@link CICBlob} whose content is provided by the given
     * {@code inputStreamSupplier}.
     * <p>
     * The supplier is expected to produce a new {@link InputStream} on every call, so that the resulting blob's content
     * can be (re-)consumed several times, e.g. for upload retries.
     *
     * @since 1.1.0
     */
    static Builder builder(Supplier<InputStream> inputStreamSupplier) {
        return new Builder(inputStreamSupplier);
    }

    /**
     * @since 1.1.0
     */
    final class Builder {

        private final Supplier<InputStream> inputStreamSupplier;

        private String contentType;

        private String name;

        private Long size;

        private String digest;

        private Builder(Supplier<InputStream> inputStreamSupplier) {
            this.inputStreamSupplier = Objects.requireNonNull(inputStreamSupplier,
                    "inputStreamSupplier cannot be null");
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * @throws IllegalArgumentException if {@code size} is negative
         */
        public Builder size(Long size) {
            if (size != null && size < 0) {
                throw new IllegalArgumentException("size cannot be negative");
            }
            this.size = size;
            return this;
        }

        public Builder digest(String digest) {
            this.digest = digest;
            return this;
        }

        public CICBlob build() {
            var finalContentType = contentType;
            var finalName = name;
            var finalSize = size;
            var finalDigest = digest;
            return new CICBlob() {
                @Override
                public InputStream getInputStream() {
                    return Objects.requireNonNull(inputStreamSupplier.get(),
                            "inputStreamSupplier cannot return a null InputStream");
                }

                @Override
                public Optional<String> getDigest() {
                    return Optional.ofNullable(finalDigest);
                }

                @Override
                public Optional<String> getContentType() {
                    return Optional.ofNullable(finalContentType);
                }

                @Override
                public Optional<String> getName() {
                    return Optional.ofNullable(finalName);
                }

                @Override
                public OptionalLong getSize() {
                    return finalSize == null ? OptionalLong.empty() : OptionalLong.of(finalSize);
                }
            };
        }
    }
}
