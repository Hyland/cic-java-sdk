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

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * @since 1.0.0
 */
public interface CICBlob {

    InputStream getInputStream();

    Optional<String> getDigest();

    default Optional<String> getContentType() {
        return Optional.empty();
    }

    /**
     * @since 1.1.0
     */
    static Builder builder(InputStream inputStream) {
        return new Builder(inputStream);
    }

    final class Builder {

        protected final InputStream inputStream;

        protected String contentType;

        protected String name;

        protected Long size;

        protected String digest;

        protected Builder(InputStream inputStream) {
            this.inputStream = Objects.requireNonNull(inputStream, "inputStream cannot be null");
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder size(Long size) {
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
                    return inputStream;
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

                @Override
                public Optional<String> getDigest() {
                    return Optional.ofNullable(finalDigest);
                }
            };
        }
    }
}
