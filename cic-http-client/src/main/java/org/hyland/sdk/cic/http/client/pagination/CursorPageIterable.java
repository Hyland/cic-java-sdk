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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A lazily-fetching {@link Iterable} over all items across cursor-paginated API responses.
 * <p>
 * Pages are fetched on demand: the first page is fetched when {@link #iterator()} is called, and each subsequent page
 * is fetched only when the current page is exhausted and
 * {@link org.hyland.sdk.cic.http.client.pagination.CursorPagination#hasMore()} is {@code true}.
 * <p>
 *
 * @param <T> the type of items returned by each page
 * @since 1.0.0
 */
public final class CursorPageIterable<T> implements Iterable<T> {

    private final Function<String, CursorPageableResponse<T>> pageSupplier;

    /**
     * Creates a new {@link CursorPageIterable}.
     *
     * @param pageSupplier a function that fetches a page given a cursor ({@code null} for the first page)
     * @throws NullPointerException if pageSupplier is null
     */
    public CursorPageIterable(Function<String, CursorPageableResponse<T>> pageSupplier) {
        this.pageSupplier = Objects.requireNonNull(pageSupplier, "pageSupplier cannot be null");
    }

    /**
     * Returns a new iterator starting from the first page.
     *
     * @return a new iterator over all items across pages
     */
    @Override
    public Iterator<T> iterator() {
        return new CursorPageIterator<>(pageSupplier);
    }

    /**
     * Returns a sequential {@link Stream} over all items across pages.
     *
     * @return a lazily-populated stream
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private static final class CursorPageIterator<T> implements Iterator<T> {

        private final Function<String, CursorPageableResponse<T>> pageSupplier;

        private Iterator<T> current;

        private String nextCursor;

        private boolean hasMore;

        CursorPageIterator(Function<String, CursorPageableResponse<T>> pageSupplier) {
            this.pageSupplier = pageSupplier;
            fetch(null);
        }

        private void fetch(String cursor) {
            var page = pageSupplier.apply(cursor);
            current = page.data().iterator();
            nextCursor = page.pagination().nextCursor();
            hasMore = page.pagination().hasMore();
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && hasMore) {
                fetch(nextCursor);
            }
            return current.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }
}
