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
package org.hyland.sdk.cic.nucleus;

import java.util.Objects;

import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;
import org.hyland.sdk.cic.nucleus.object.PaginatedList;

/**
 * High-level service for CIC Users API operations.
 *
 * @since 1.0.0
 */
public class UsersService {

    protected final NucleusHttpClient httpClient;

    public UsersService(NucleusHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public InteractiveUser.PaginatedListOf listUsers() {
        return httpClient.listUsers(null, null, null);
    }

    public InteractiveUser.PaginatedListOf listUsers(String externalId, String cursor, Integer limit) {
        return httpClient.listUsers(externalId, cursor, limit);
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all users across all pages.
     *
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<InteractiveUser> listUsersPaginator() {
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listUsers(null, cursor, null)));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over users filtered by external ID across all pages.
     *
     * @param externalId the external ID to filter by
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<InteractiveUser> listUsersPaginator(String externalId) {
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listUsers(externalId, cursor, null)));
    }

    public InteractiveUser getUser(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        return httpClient.getUser(userId);
    }

    private static <T> CursorPageableResponse<T> toPageableResponse(PaginatedList<T> page) {
        var next = page.next();
        return new CursorPageableResponse<>(page.items(), new CursorPagination(next, next != null));
    }

    public CursorPageIterable<InteractiveUser> listUsersPaginator(Integer limit) {
        return new CursorPageIterable<>(cursor -> toPageableResponse(httpClient.listUsers(null, cursor, limit)));
    }
}
