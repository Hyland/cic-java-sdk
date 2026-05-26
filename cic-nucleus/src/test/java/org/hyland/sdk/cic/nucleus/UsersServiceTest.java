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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.nucleus.object.InteractiveUser;

/**
 * @since 1.0.0
 */
class UsersServiceTest {

    private static final String USER_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";

    private TestNucleusHttpClient httpClient;

    private UsersService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestNucleusHttpClient();
        service = new UsersService(httpClient);
    }

    @Test
    void testListUsers() {
        httpClient.usersPaginated = new InteractiveUser.PaginatedListOf(
                List.of(new InteractiveUser(USER_ID, "alice", "alice@localhost", "ext-alice", "en-US")), null);

        var result = service.listUsers();

        assertEquals(1, result.items().size());
        assertEquals("alice", result.items().get(0).userName());
        assertEquals(USER_ID, result.items().get(0).userId());
    }

    @Test
    void testListUsersWithParams() {
        httpClient.usersPaginated = new InteractiveUser.PaginatedListOf(List.of(), null);

        service.listUsers("ext-alice", "cursor-1", 10);

        assertEquals("ext-alice", httpClient.lastExternalId);
        assertEquals("cursor-1", httpClient.lastCursor);
        assertEquals(10, httpClient.lastLimit);
    }

    @Test
    void testListUsersPaginator() {
        httpClient.usersPaginated = new InteractiveUser.PaginatedListOf(
                List.of(new InteractiveUser(USER_ID, "alice", "alice@localhost", "ext-alice", null)), null);

        var items = new ArrayList<InteractiveUser>();
        service.listUsersPaginator().forEach(items::add);

        assertEquals(1, items.size());
        assertEquals("alice", items.get(0).userName());
    }

    @Test
    void testListUsersPaginatorMultiplePages() {
        var user1 = new InteractiveUser("user-id-1", "alice", "alice@localhost", null, null);
        var user2 = new InteractiveUser("user-id-2", "bob", "bob@localhost", null, null);

        httpClient.usersPaginatedPages = List.of(new InteractiveUser.PaginatedListOf(List.of(user1), "cursor-2"),
                new InteractiveUser.PaginatedListOf(List.of(user2), null));

        var items = new ArrayList<InteractiveUser>();
        service.listUsersPaginator().forEach(items::add);

        assertEquals(2, items.size());
        assertEquals("alice", items.get(0).userName());
        assertEquals("bob", items.get(1).userName());
    }

    @Test
    void testListUsersPaginatorWithExternalId() {
        httpClient.usersPaginated = new InteractiveUser.PaginatedListOf(List.of(), null);

        service.listUsersPaginator("ext-alice").forEach(u -> {
        });

        assertEquals("ext-alice", httpClient.lastExternalId);
    }

    @Test
    void testGetUser() {
        httpClient.user = new InteractiveUser(USER_ID, "alice", "alice@localhost", "ext-alice", "en-US");

        var result = service.getUser(USER_ID);

        assertEquals(USER_ID, result.userId());
        assertEquals("alice", result.userName());
        assertEquals(USER_ID, httpClient.lastUserId);
    }

    @Test
    void testGetUserNullIdThrows() {
        assertThrows(NullPointerException.class, () -> service.getUser(null));
    }

    private static class TestNucleusHttpClient extends NucleusHttpClient {

        InteractiveUser.PaginatedListOf usersPaginated;

        List<InteractiveUser.PaginatedListOf> usersPaginatedPages;

        int pageIndex;

        InteractiveUser user;

        String lastExternalId;

        String lastCursor;

        Integer lastLimit;

        String lastUserId;

        public TestNucleusHttpClient() {
            super(NucleusHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public InteractiveUser.PaginatedListOf listUsers(String externalId, String cursor, Integer limit) {
            lastExternalId = externalId;
            lastCursor = cursor;
            lastLimit = limit;
            if (usersPaginatedPages != null && pageIndex < usersPaginatedPages.size()) {
                return usersPaginatedPages.get(pageIndex++);
            }
            return usersPaginated;
        }

        @Override
        public InteractiveUser getUser(String userId) {
            lastUserId = userId;
            return user;
        }
    }
}
