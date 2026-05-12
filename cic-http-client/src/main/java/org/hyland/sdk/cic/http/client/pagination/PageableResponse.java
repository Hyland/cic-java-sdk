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

import java.util.List;
import java.util.Objects;

/**
 * A page of offset-paginated results returned by the CIC API.
 *
 * @param <T> the type of items in this page
 * @since 1.0.0
 */
public class PageableResponse<T> {

    private final List<T> data;

    private final Pagination pagination;

    public PageableResponse(List<T> data, Pagination pagination) {
        Objects.requireNonNull(data, "data cannot be null");
        Objects.requireNonNull(pagination, "pagination cannot be null");
        this.data = List.copyOf(data);
        this.pagination = pagination;
    }

    public List<T> data() {
        return data;
    }

    public Pagination pagination() {
        return pagination;
    }
}
