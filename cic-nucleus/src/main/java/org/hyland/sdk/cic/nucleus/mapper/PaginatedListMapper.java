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
import java.util.function.BiFunction;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.nucleus.object.PaginatedList;

/**
 * Generic paginated-list mapper. Reads a page envelope with an items array and a {@code next} cursor, delegating each
 * item to the provided item mapper and constructing the result via the provided factory.
 *
 * @param <T> the item type
 * @param <L> the paginated list type
 * @since 1.0.0
 */
class PaginatedListMapper<T, L extends PaginatedList<T>> implements CICMapper<L> {

    private final CICMapper<T> itemMapper;

    private final String itemsKey;

    private final BiFunction<List<T>, String, L> factory;

    PaginatedListMapper(CICMapper<T> itemMapper, String itemsKey, BiFunction<List<T>, String, L> factory) {
        this.itemMapper = itemMapper;
        this.itemsKey = itemsKey;
        this.factory = factory;
    }

    @Override
    public L fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        var items = obj.getOptionalArray(itemsKey)
                       .map(a -> a.toListObject().stream().map(itemMapper::fromCICNode).toList())
                       .orElse(null);
        var next = obj.getString("next", null);
        return factory.apply(items, next);
    }
}
