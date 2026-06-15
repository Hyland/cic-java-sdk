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

import java.util.List;
import java.util.Objects;

/**
 * @since 1.0.0
 */
public record PrincipalUserMapping(String systemId, String externalUserId, List<Attribute> attributes) {

    public PrincipalUserMapping {
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(externalUserId, "externalUserId cannot be null");
        attributes = attributes != null ? List.copyOf(attributes) : List.of();
    }

}
