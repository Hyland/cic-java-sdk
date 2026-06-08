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
import java.util.function.Consumer;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;
import org.hyland.sdk.cic.nucleus.object.InteractiveUserPage;
import org.hyland.sdk.cic.nucleus.object.ListUsersRequest;

/**
 * High-level service for CIC Users API operations.
 *
 * @since 1.0.0
 */
public class UsersService {

    protected final NucleusIAMHttpClient httpClient;

    public UsersService(NucleusIAMHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Lists all users.
     *
     * @return the first page of users
     * @throws CICSdkException if the request fails
     */
    public InteractiveUserPage listUsers() {
        return httpClient.listUsers(null, null, null);
    }

    /**
     * Lists users using a builder consumer to specify optional filter and pagination parameters.
     *
     * @param consumer configures optional parameters (externalId, cursor, limit)
     * @return the requested page of users
     * @throws NullPointerException if consumer is null
     * @throws CICSdkException if the request fails
     */
    public InteractiveUserPage listUsers(Consumer<ListUsersRequest.Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        var builder = ListUsersRequest.builder();
        consumer.accept(builder);
        var request = builder.build();
        return httpClient.listUsers(request.externalId(), request.cursor(), request.limit());
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all users across all pages.
     *
     * @return an iterable that fetches pages on demand
     */
    public CursorPageIterable<InteractiveUser> listUsersPaginator() {
        return new CursorPageIterable<>(cursor -> httpClient.listUsers(null, cursor, null));
    }

    /**
     * Returns a lazily-fetching {@link Iterable} over all users across all pages, using a builder consumer to specify
     * optional filter and pagination parameters.
     * <p>
     * The {@code cursor} field of the request is ignored; the paginator manages the cursor internally.
     *
     * @param consumer configures optional parameters (externalId, limit)
     * @return an iterable that fetches pages on demand
     * @throws NullPointerException if consumer is null
     */
    public CursorPageIterable<InteractiveUser> listUsersPaginator(Consumer<ListUsersRequest.Builder> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        var builder = ListUsersRequest.builder();
        consumer.accept(builder);
        var request = builder.build();
        return new CursorPageIterable<>(cursor -> httpClient.listUsers(request.externalId(), cursor, request.limit()));
    }

    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the interactive user
     * @throws NullPointerException if userId is null
     * @throws CICSdkException if the request fails
     */
    public InteractiveUser getUser(String userId) {
        Objects.requireNonNull(userId, "userId cannot be null");
        return httpClient.getUser(userId);
    }

}
