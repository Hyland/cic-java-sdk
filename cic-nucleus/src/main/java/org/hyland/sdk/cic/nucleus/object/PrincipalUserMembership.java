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
import java.util.UUID;

/**
 * @since 1.0.0
 */
public record PrincipalUserMembership(String externalGroupId, UUID systemId, MembershipType membershipType,
        List<Attribute> attributes) {

    public PrincipalUserMembership {
        Objects.requireNonNull(externalGroupId, "externalGroupId cannot be null");
        Objects.requireNonNull(systemId, "systemId cannot be null");
        Objects.requireNonNull(membershipType, "membershipType cannot be null");
        Objects.requireNonNull(attributes, "attributes cannot be null");
        attributes = List.copyOf(attributes);
    }

    public static class PaginatedListOf extends PaginatedList<PrincipalUserMembership> {

        public PaginatedListOf(List<PrincipalUserMembership> items, String next) {
            super(items, next);
        }
    }
}
