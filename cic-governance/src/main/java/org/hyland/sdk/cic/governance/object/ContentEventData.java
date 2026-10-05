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
package org.hyland.sdk.cic.governance.object;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.hyland.sdk.cic.http.client.util.StringUtils;

/**
 * The {@code data} payload of a {@link ContentEvent}.
 *
 * @since 1.2.0
 */
public final class ContentEventData {

    protected final String contentId;

    protected final String environmentId;

    protected final String fileName;

    protected final String contentFocus;

    protected final Map<String, String> metadata;

    protected ContentEventData(Builder builder) {
        this.contentId = StringUtils.requireNonBlank(builder.contentId, "contentId cannot be blank");
        this.environmentId = StringUtils.requireNonBlank(builder.environmentId, "environmentId cannot be blank");
        this.fileName = StringUtils.requireNonBlank(builder.fileName, "fileName cannot be blank");
        this.contentFocus = StringUtils.requireNonBlank(builder.contentFocus, "contentFocus cannot be blank");
        this.metadata = Map.copyOf(builder.metadata);
    }

    /**
     * @param contentId the document id on the repository side; CIC Governance echoes it back when requesting a
     *            retention action, so the repository can locate the document and apply the retention
     */
    public static Builder builder(String contentId, String environmentId, String fileName, String contentFocus) {
        return new Builder(contentId, environmentId, fileName, contentFocus);
    }

    /**
     * The document id on the repository side. CIC Governance echoes this id back when requesting a retention action on
     * the content, so the repository can use it to locate the document and apply the retention.
     */
    public String contentId() {
        return contentId;
    }

    public String environmentId() {
        return environmentId;
    }

    public String fileName() {
        return fileName;
    }

    public String contentFocus() {
        return contentFocus;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public Builder toBuilder() {
        return new Builder(contentId, environmentId, fileName, contentFocus).metadata(metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (ContentEventData) obj;
        return Objects.equals(this.contentId, that.contentId) && Objects.equals(this.environmentId, that.environmentId)
                && Objects.equals(this.fileName, that.fileName) && Objects.equals(this.contentFocus, that.contentFocus)
                && Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId, environmentId, fileName, contentFocus, metadata);
    }

    @Override
    public String toString() {
        return "ContentEventData[contentId=" + contentId + ", environmentId=" + environmentId + ", fileName=" + fileName
                + ", contentFocus=" + contentFocus + ", metadata=" + metadata + "]";
    }

    public static final class Builder {

        private final String contentId;

        private final String environmentId;

        private final String fileName;

        private final String contentFocus;

        private Map<String, String> metadata = new LinkedHashMap<>();

        private Builder(String contentId, String environmentId, String fileName, String contentFocus) {
            this.contentId = Objects.requireNonNull(contentId, "contentId cannot be null");
            this.environmentId = Objects.requireNonNull(environmentId, "environmentId cannot be null");
            this.fileName = Objects.requireNonNull(fileName, "fileName cannot be null");
            this.contentFocus = Objects.requireNonNull(contentFocus, "contentFocus cannot be null");
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = new LinkedHashMap<>(Objects.requireNonNull(metadata, "metadata cannot be null"));
            return this;
        }

        public Builder putMetadata(String key, String value) {
            this.metadata.put(Objects.requireNonNull(key, "key cannot be null"), value);
            return this;
        }

        public ContentEventData build() {
            return new ContentEventData(this);
        }
    }
}
