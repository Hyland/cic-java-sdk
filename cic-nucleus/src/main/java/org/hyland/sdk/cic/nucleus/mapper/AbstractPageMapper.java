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

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.nucleus.pagination.NucleusCursorExtractor;

/**
 * @since 1.0.0
 */
abstract class AbstractPageMapper<T, P> implements CICMapper<P> {

    private final CICMapper<T> itemMapper;

    private final String arrayField;

    protected AbstractPageMapper(CICMapper<T> itemMapper) {
        this(itemMapper, "items");
    }

    protected AbstractPageMapper(CICMapper<T> itemMapper, String arrayField) {
        this.itemMapper = itemMapper;
        this.arrayField = arrayField;
    }

    @Override
    public P fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var data = obj.getOptionalArray(arrayField)
                      .map(a -> a.toListObject().stream().map(itemMapper::fromCICNode).toList())
                      .orElse(List.of());
        var cursor = NucleusCursorExtractor.extract(obj.getString("next", null));
        return createPage(data, new CursorPagination(cursor, cursor != null));
    }

    protected abstract P createPage(List<T> data, CursorPagination pagination);
}
