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
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A lazily-fetching {@link Iterable} over all items across offset-paginated API responses.
 * <p>
 * Pages are fetched on demand starting at page 1. Each subsequent page is fetched only when the current page is
 * exhausted and the current page number is less than {@link Pagination#totalPages()}.
 * <p>
 *
 * @param <T> the type of items returned by each page
 * @since 1.0.0
 */
public final class PageIterable<T> implements Iterable<T> {

    private final IntFunction<PageableResponse<T>> pageSupplier;

    /**
     * Creates a new {@link PageIterable}.
     *
     * @param pageSupplier a function that fetches a page given a 1-based page number
     * @throws NullPointerException if pageSupplier is null
     */
    public PageIterable(IntFunction<PageableResponse<T>> pageSupplier) {
        this.pageSupplier = Objects.requireNonNull(pageSupplier, "pageSupplier cannot be null");
    }

    /**
     * Returns a new iterator starting from page 1. Each call returns an independent iterator.
     *
     * @return a new iterator over all items across pages
     */
    @Override
    public Iterator<T> iterator() {
        return new PageIterator<>(pageSupplier);
    }

    /**
     * Returns a sequential {@link Stream} over all items across pages.
     *
     * @return a lazily-populated stream
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private static final class PageIterator<T> implements Iterator<T> {

        private final IntFunction<PageableResponse<T>> pageSupplier;

        private Iterator<T> current;

        private int nextPage;

        private boolean hasMore;

        PageIterator(IntFunction<PageableResponse<T>> pageSupplier) {
            this.pageSupplier = pageSupplier;
            fetch(1);
        }

        private void fetch(int pageNumber) {
            var page = pageSupplier.apply(pageNumber);
            current = page.data().iterator();
            nextPage = page.pagination().pageNumber() + 1;
            hasMore = page.pagination().pageNumber() < page.pagination().totalPages();
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && hasMore) {
                fetch(nextPage);
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
