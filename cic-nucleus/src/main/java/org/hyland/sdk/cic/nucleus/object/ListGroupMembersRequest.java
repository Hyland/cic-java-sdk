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
package org.hyland.sdk.cic.nucleus.object;

/**
 * Optional parameters for listing group members with cursor-based pagination.
 *
 * @since 1.0.0
 */
public record ListGroupMembersRequest(String externalGroupId, String cursor, Integer limit) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String externalGroupId;

        private String cursor;

        private Integer limit;

        private Builder() {
        }

        public Builder externalGroupId(String externalGroupId) {
            this.externalGroupId = externalGroupId;
            return this;
        }

        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public ListGroupMembersRequest build() {
            return new ListGroupMembersRequest(externalGroupId, cursor, limit);
        }
    }
}
