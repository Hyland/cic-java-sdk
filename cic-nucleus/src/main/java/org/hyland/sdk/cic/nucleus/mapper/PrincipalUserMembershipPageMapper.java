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
package org.hyland.sdk.cic.nucleus.mapper;

import java.util.List;

import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembership;
import org.hyland.sdk.cic.nucleus.object.PrincipalUserMembershipPage;

/**
 * @since 1.0.0
 */
class PrincipalUserMembershipPageMapper
        extends AbstractPageMapper<PrincipalUserMembership, PrincipalUserMembershipPage> {

    PrincipalUserMembershipPageMapper() {
        super(new PrincipalUserMembershipMapper());
    }

    @Override
    protected PrincipalUserMembershipPage createPage(List<PrincipalUserMembership> data, CursorPagination pagination) {
        return new PrincipalUserMembershipPage(data, pagination);
    }
}
