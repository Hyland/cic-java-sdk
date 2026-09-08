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

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;

/**
 * @since 1.1.0
 */
public final class IngestEventPropertyFile implements IngestEventProperty {

    // could be present during service execution, should be uploaded and removed before client ingestion
    protected final CICBlob blob;

    protected final String id;

    protected final String contentType;

    protected final String name;

    protected final Long size;

    protected final String digest;

    protected IngestEventPropertyFile(Builder builder) {
        this.blob = builder.blob;
        this.id = builder.id;
        this.contentType = builder.contentType;
        this.name = builder.name;
        this.size = builder.size;
        this.digest = builder.digest;
    }

    public Optional<CICBlob> blob() {
        return Optional.ofNullable(blob);
    }

    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    public Optional<String> contentType() {
        return Optional.ofNullable(contentType);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public OptionalLong size() {
        return size == null ? OptionalLong.empty() : OptionalLong.of(size);
    }

    public Optional<String> digest() {
        return Optional.ofNullable(digest);
    }

    /**
     * @implNote the returned builder will not hold the blob if any
     */
    public Builder toBuilder() {
        return builder().id(id).contentType(contentType).name(name).size(size).digest(digest);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(CICBlob blob) {
        return new Builder(blob);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IngestEventPropertyFile other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "{id=%s,contentType=%s}".formatted(id, contentType);
    }

    public static final class Builder {

        protected final CICBlob blob;

        protected String id;

        protected String contentType;

        protected String name;

        protected Long size;

        protected String digest;

        public Builder() {
            this.blob = null;
        }

        public Builder(CICBlob blob) {
            this.blob = Objects.requireNonNull(blob, "blob cannot be null");
            this.contentType = blob.getContentType().orElse(null);
            this.name = blob.getName().orElse(null);
            this.size = blob.getSize().isPresent() ? blob.getSize().getAsLong() : null;
            this.digest = blob.getDigest().orElse(null);
        }

        public Builder id(String id) {
            this.id = id;
            return this;
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

        public IngestEventPropertyFile build() {
            // validate
            if (blob != null && contentType == null) {
                throw new IllegalArgumentException("contentType must be provided if blob is");
            }
            if ((size != null && (name == null || contentType == null)) //
                    || (name != null && (size == null || contentType == null))) {
                throw new IllegalArgumentException(
                        "metadata(size, name, contentType) should be provided together or not at all");
            }
            // build
            return new IngestEventPropertyFile(this);
        }
    }
}
