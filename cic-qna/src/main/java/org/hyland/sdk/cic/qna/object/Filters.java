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
 * @since 1.0.0
 */
public record Filters(FilterExpression merged, FilterExpression dynamic, FilterExpression staticFilter,
        String hxqlFilter) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private FilterExpression merged;

        private FilterExpression dynamic;

        private FilterExpression staticFilter;

        private String hxqlFilter;

        private Builder() {
        }

        public Builder merged(FilterExpression merged) {
            this.merged = merged;
            return this;
        }

        public Builder dynamic(FilterExpression dynamic) {
            this.dynamic = dynamic;
            return this;
        }

        public Builder staticFilter(FilterExpression staticFilter) {
            this.staticFilter = staticFilter;
            return this;
        }

        public Builder hxqlFilter(String hxqlFilter) {
            this.hxqlFilter = hxqlFilter;
            return this;
        }

        public Filters build() {
            return new Filters(merged, dynamic, staticFilter, hxqlFilter);
        }
    }
}
