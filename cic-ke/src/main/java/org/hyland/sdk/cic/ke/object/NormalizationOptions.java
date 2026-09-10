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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.object;

/**
 * Normalization processing options.
 *
 * @since 1.1.0
 */
public record NormalizationOptions(Boolean quotations, Boolean dashes) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Boolean quotations;

        private Boolean dashes;

        public Builder quotations(boolean quotations) {
            this.quotations = quotations;
            return this;
        }

        public Builder dashes(boolean dashes) {
            this.dashes = dashes;
            return this;
        }

        public NormalizationOptions build() {
            return new NormalizationOptions(quotations, dashes);
        }
    }
}
