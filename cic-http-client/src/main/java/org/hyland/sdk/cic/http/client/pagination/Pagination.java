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
package org.hyland.sdk.cic.http.client.pagination;

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * Offset-based pagination metadata returned by the API.
 *
 * @since 1.0.0
 */
public record Pagination(int pageSize, int pageNumber, int totalItems, int totalPages) {

    public static Pagination from(CICObject obj) {
        var paginationObj = obj.getObjectOrThrow("pagination");
        return new Pagination( //
                paginationObj.getInt("pageSize", 0), //
                paginationObj.getInt("pageNumber", 0), //
                paginationObj.getInt("totalItems", 0), //
                paginationObj.getInt("totalPages", 0));
    }
}
