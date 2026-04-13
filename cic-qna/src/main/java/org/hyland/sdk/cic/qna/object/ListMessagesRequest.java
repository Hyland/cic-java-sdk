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
package org.hyland.sdk.cic.qna.object;

/**
 * Optional parameters for listing conversation messages with cursor-based pagination.
 *
 * @since 1.0.0
 */
public record ListMessagesRequest(String cursor, Integer pageSize, Integer maxContentLength, String fields) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String cursor;

        private Integer pageSize;

        private Integer maxContentLength;

        private String fields;

        private Builder() {
        }

        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder maxContentLength(int maxContentLength) {
            this.maxContentLength = maxContentLength;
            return this;
        }

        public Builder fields(String fields) {
            this.fields = fields;
            return this;
        }

        public ListMessagesRequest build() {
            return new ListMessagesRequest(cursor, pageSize, maxContentLength, fields);
        }
    }
}
