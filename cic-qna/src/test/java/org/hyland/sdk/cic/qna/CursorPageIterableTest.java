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
package org.hyland.sdk.cic.qna;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.pagination.CursorPageIterable;
import org.hyland.sdk.cic.http.client.pagination.CursorPageableResponse;
import org.hyland.sdk.cic.http.client.pagination.CursorPagination;

/**
 * @since 1.0.0
 */
class CursorPageIterableTest {

    private static CursorPageableResponse<String> page(List<String> items, String nextCursor, boolean hasMore) {
        return new CursorPageableResponse<>(items, new CursorPagination(nextCursor, hasMore)) {
        };
    }

    @Test
    void testSinglePageIteration() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a", "b", "c"), null, false));

        var result = new ArrayList<String>();
        iterable.forEach(result::add);

        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testMultiPageIteration() {
        var pages = Map.of("cursor-1", page(List.of("d", "e"), "cursor-2", true), "cursor-2",
                page(List.of("f"), null, false));

        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null) {
                return page(List.of("a", "b", "c"), "cursor-1", true);
            }
            return pages.get(cursor);
        });

        assertEquals(List.of("a", "b", "c", "d", "e", "f"), iterable.stream().toList());
    }

    @Test
    void testEmptyFirstPage() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of(), null, false));

        var iterator = iterable.iterator();

        assertFalse(iterator.hasNext());
    }

    @Test
    void testEmptyMiddlePage() {
        // An empty page with hasMore=true should not loop forever — it should stop
        // because the next fetch returns empty with hasMore=false
        var call = new int[] { 0 };
        var iterable = new CursorPageIterable<String>(cursor -> {
            call[0]++;
            return switch (call[0]) {
                case 1 -> page(List.of("a"), "c2", true);
                case 2 -> page(List.of(), "c3", true);
                case 3 -> page(List.of("b"), null, false);
                default -> throw new IllegalStateException("unexpected call " + call[0]);
            };
        });

        assertEquals(List.of("a", "b"), iterable.stream().toList());
    }

    @Test
    void testCursorIsPassedBetweenPages() {
        var capturedCursors = new ArrayList<String>();

        var iterable = new CursorPageIterable<String>(cursor -> {
            capturedCursors.add(cursor);
            if (cursor == null) {
                return page(List.of("x"), "next-cursor", true);
            }
            return page(List.of("y"), null, false);
        });

        iterable.stream().toList();

        assertEquals(2, capturedCursors.size());
        assertNull(capturedCursors.get(0));
        assertEquals("next-cursor", capturedCursors.get(1));
    }

    @Test
    void testHasNextIsIdempotent() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("only"), null, false));

        var iterator = iterable.iterator();

        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());
        assertEquals("only", iterator.next());
        assertFalse(iterator.hasNext());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testNextThrowsWhenExhausted() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("item"), null, false));

        var iterator = iterable.iterator();
        iterator.next();

        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testNextThrowsOnEmptyPage() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of(), null, false));

        assertThrows(NoSuchElementException.class, iterable.iterator()::next);
    }

    @Test
    void testForEachLoop() {
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("a", "b"), "c2", true);
            return page(List.of("c"), null, false);
        });

        var result = new ArrayList<String>();
        for (var item : iterable) {
            result.add(item);
        }

        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void testForEachConsumer() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("x", "y", "z"), null, false));

        var result = new ArrayList<String>();
        iterable.forEach(result::add);

        assertEquals(List.of("x", "y", "z"), result);
    }

    @Test
    void testReentrant_twoSequentialFullIterations() {
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("a", "b"), "c2", true);
            return page(List.of("c"), null, false);
        });

        var first = new ArrayList<String>();
        iterable.forEach(first::add);

        var second = new ArrayList<String>();
        iterable.forEach(second::add);

        assertEquals(List.of("a", "b", "c"), first);
        assertEquals(first, second);
    }

    @Test
    void testSpliterator() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("p", "q", "r"), null, false));

        var result = new ArrayList<String>();
        iterable.spliterator().forEachRemaining(result::add);

        assertEquals(List.of("p", "q", "r"), result);
    }

    // ---- stream() contract ----

    @Test
    void testStreamIsSequential() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a"), null, false));

        assertFalse(iterable.stream().isParallel());
    }

    @Test
    void testStreamFilter() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a", "b", "c"), null, false));

        assertEquals(List.of("a", "c"), iterable.stream().filter(s -> !s.equals("b")).toList());
    }

    @Test
    void testStreamMap() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a", "b", "c"), null, false));

        assertEquals(List.of("A", "B", "C"), iterable.stream().map(String::toUpperCase).toList());
    }

    @Test
    void testStreamCount() {
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("1", "2"), "next", true);
            return page(List.of("3"), null, false);
        });

        assertEquals(3, iterable.stream().count());
    }

    @Test
    void testStreamCollect() {
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("a", "b"), "c2", true);
            return page(List.of("c"), null, false);
        });

        assertEquals(List.of("a", "b", "c"), iterable.stream().collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void testStreamReentrant_twoSequentialStreams() {
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("a", "b"), "c2", true);
            return page(List.of("c"), null, false);
        });

        assertEquals(List.of("a", "b", "c"), iterable.stream().toList());
        assertEquals(List.of("a", "b", "c"), iterable.stream().toList());
    }

    @Test
    void testStreamFindFirstFetchesOnlyFirstPage() {
        var pagesFetched = new int[] { 0 };
        var iterable = new CursorPageIterable<String>(cursor -> {
            pagesFetched[0]++;
            if (cursor == null)
                return page(List.of("first", "second"), "c2", true);
            return page(List.of("third"), null, false);
        });

        var found = iterable.stream().findFirst();

        assertTrue(found.isPresent());
        assertEquals("first", found.get());
        assertEquals(1, pagesFetched[0]);
    }

    @Test
    void testStreamLimitDoesNotFetchUnnecessaryPages() {
        var pagesFetched = new int[] { 0 };
        // 3 pages of 3 items each; limit(4) needs only pages 1 and 2
        var iterable = new CursorPageIterable<String>(cursor -> {
            pagesFetched[0]++;
            return switch (pagesFetched[0]) {
                case 1 -> page(List.of("a", "b", "c"), "c2", true);
                case 2 -> page(List.of("d", "e", "f"), "c3", true);
                case 3 -> page(List.of("g", "h", "i"), null, false);
                default -> throw new IllegalStateException("unexpected fetch " + pagesFetched[0]);
            };
        });

        var result = iterable.stream().limit(4).toList();

        assertEquals(List.of("a", "b", "c", "d"), result);
        assertEquals(2, pagesFetched[0]);
    }

    @Test
    void testStreamAnyMatchStopsEarly() {
        var pagesFetched = new int[] { 0 };
        var iterable = new CursorPageIterable<String>(cursor -> {
            pagesFetched[0]++;
            if (cursor == null)
                return page(List.of("no", "yes", "no"), "c2", true);
            return page(List.of("no"), null, false);
        });

        assertTrue(iterable.stream().anyMatch("yes"::equals));
        assertEquals(1, pagesFetched[0]);
    }

    @Test
    void testHasMoreTrueWithNullCursorThrows() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a"), null, true));

        assertThrows(IllegalStateException.class, () -> iterable.stream().toList());
    }

    @Test
    void testHasMoreTrueWithBlankCursorThrows() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a"), "  ", true));

        assertThrows(IllegalStateException.class, () -> iterable.stream().toList());
    }

    @Test
    void testNullPageSupplierThrows() {
        assertThrows(NullPointerException.class, () -> new CursorPageIterable<>(null));
    }

    @Test
    void testEachIteratorCallReturnsIndependentInstance() {
        var iterable = new CursorPageIterable<>(cursor -> page(List.of("a"), null, false));

        var it1 = iterable.iterator();
        var it2 = iterable.iterator();

        assertNotSame(it1, it2);
        // consuming one does not affect the other
        it1.next();
        assertFalse(it1.hasNext());
        assertTrue(it2.hasNext());
    }

    @Test
    void testConcurrentIteratorsAreIndependent() throws InterruptedException {
        int threadCount = 8;
        var iterable = new CursorPageIterable<String>(cursor -> {
            if (cursor == null)
                return page(List.of("p1-a", "p1-b"), "c2", true);
            return page(List.of("p2-a", "p2-b"), null, false);
        });

        var results = new ConcurrentHashMap<Integer, List<String>>();
        var latch = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            pool.submit(() -> {
                try {
                    latch.countDown();
                    latch.await(); // all threads start at the same time
                    var collected = iterable.stream().toList();
                    results.put(threadId, collected);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(threadCount, results.size());
        var expected = List.of("p1-a", "p1-b", "p2-a", "p2-b");
        results.values().forEach(r -> assertEquals(expected, r));
    }

    @Test
    void testListMessagesPaginatorDelegatesCorrectly() {
        var service = new QnaService(new StubQnaHttpClient());
        var iterable = service.agent("agent-1").conversation("conv-1").listMessagesPaginator();

        var items = iterable.stream().toList();

        assertEquals(2, items.size());
        assertEquals("msg-1", items.get(0).id());
        assertEquals("msg-2", items.get(1).id());
    }

    @Test
    void testListConversationsPaginatorDelegatesCorrectly() {
        var service = new QnaService(new StubQnaHttpClient());
        var iterable = service.agent("agent-1").listConversationsPaginator();

        var items = iterable.stream().toList();

        assertEquals(2, items.size());
        assertEquals("conv-1", items.get(0).id());
        assertEquals("conv-2", items.get(1).id());
    }

    private static class StubQnaHttpClient extends QnaHttpClient {

        StubQnaHttpClient() {
            super(QnaHttpClient.from("https://localhost",
                    org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient.from()
                                                                                .clientId("test")
                                                                                .clientSecret("test")));
        }

        @Override
        public CursorPageableResponse<org.hyland.sdk.cic.qna.object.ConversationMessage> listMessages(String agentId,
                String conversationId, String cursor, Integer pageSize, Integer maxContentLength, String fields) {
            if (cursor == null) {
                return page(List.of(msg("msg-1"), msg("msg-2")), null, false);
            }
            return page(List.of(), null, false);
        }

        @Override
        public CursorPageableResponse<org.hyland.sdk.cic.qna.object.Conversation> listConversations(String agentId,
                String cursor, Integer pageSize) {
            if (cursor == null) {
                return page(List.of(conv("conv-1"), conv("conv-2")), null, false);
            }
            return page(List.of(), null, false);
        }

        private static <T> CursorPageableResponse<T> page(List<T> items, String nextCursor, boolean hasMore) {
            return new CursorPageableResponse<>(items, new CursorPagination(nextCursor, hasMore)) {
            };
        }

        private static org.hyland.sdk.cic.qna.object.ConversationMessage msg(String id) {
            return new org.hyland.sdk.cic.qna.object.ConversationMessage(id, "Q?", null, List.of(), List.of(), null,
                    null, null, null, null, 1, org.hyland.sdk.cic.qna.object.MessageStatus.SUBMITTED);
        }

        private static org.hyland.sdk.cic.qna.object.Conversation conv(String id) {
            return new org.hyland.sdk.cic.qna.object.Conversation(id, "Conv " + id, null, "2026-04-08T00:00:00Z");
        }
    }
}
