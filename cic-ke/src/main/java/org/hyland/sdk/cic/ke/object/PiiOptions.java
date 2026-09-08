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
 * PII processing options.
 *
 * @since 1.1.0
 */
public record PiiOptions(String mode, Boolean entityRedaction) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String mode;

        private Boolean entityRedaction;

        public Builder mode(String mode) {
            this.mode = mode;
            return this;
        }

        public Builder entityRedaction(boolean entityRedaction) {
            this.entityRedaction = entityRedaction;
            return this;
        }

        public PiiOptions build() {
            return new PiiOptions(mode, entityRedaction);
        }
    }
}
