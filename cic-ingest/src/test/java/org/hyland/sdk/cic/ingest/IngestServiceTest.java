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
package org.hyland.sdk.cic.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyFile;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

/**
 * @since 1.0.0
 */
class IngestServiceTest {

    private TestIngestHttpClient httpClient;

    private IngestService service;

    @BeforeEach
    void setUp() {
        httpClient = new TestIngestHttpClient();
        service = new IngestService(httpClient);
    }

    @Test
    void testIngest() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("title", "Test Document")
                               .build();

        service.ingest(event);

        assertEquals(1, httpClient.ingestCalls.size());
        var batch = httpClient.ingestCalls.get(0);
        assertEquals(1, batch.size());
        assertEquals(event, batch.get(0));
    }

    @Test
    void testIngestBatch() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("title", "Test Document")
                               .build();

        service.ingest(IngestEvent.Batch.of(event));

        assertEquals(1, httpClient.ingestCalls.size());
        var batch = httpClient.ingestCalls.get(0);
        assertEquals(1, batch.size());
        assertEquals(event, batch.get(0));
    }

    @Test
    void testIngestBatchWithMultipleEvents() {
        var event1 = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                                .date(Instant.ofEpochMilli(1609459200000L))
                                .putProperty("title", "First")
                                .build();
        var event2 = IngestEvent.builder(IngestEvent.Type.UPDATE, "doc2")
                                .sourceId("custom-source")
                                .date(Instant.ofEpochMilli(1609459200000L))
                                .putProperty("title", "Second")
                                .build();

        service.ingest(IngestEvent.Batch.of(event1, event2));

        assertEquals(1, httpClient.ingestCalls.size());
        var batch = httpClient.ingestCalls.get(0);
        assertEquals(2, batch.size());
        assertEquals(event1, batch.get(0));
        assertEquals(event2, batch.get(1));
    }

    @Test
    void testIngestBatchUploadsBlobsForEachEvent() {
        CICBlob blob1 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();
        CICBlob blob2 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();
        var event1 = IngestEvent.builder(IngestEvent.Type.CREATE, "doc1")
                                .putProperty("file:content",
                                        IngestEventPropertyFile.builder(blob1)
                                                               .contentType("text/plain")
                                                               .name("first.txt")
                                                               .size(11L)
                                                               .build())
                                .build();
        var event2 = IngestEvent.builder(IngestEvent.Type.CREATE, "doc2")
                                .putProperty("file:content",
                                        IngestEventPropertyFile.builder(blob2)
                                                               .contentType("application/json")
                                                               .name("second.json")
                                                               .size(22L)
                                                               .build())
                                .build();

        httpClient.preSignedUrls.add(new PreSignedUrl("id-1", "https://localhost/upload1"));
        httpClient.preSignedUrls.add(new PreSignedUrl("id-2", "https://localhost/upload2"));

        service.ingest(IngestEvent.Batch.of(event1, event2));

        assertEquals(2, httpClient.uploadCalls.size());
        assertEquals(1, httpClient.ingestCalls.size());
        var batch = httpClient.ingestCalls.get(0);
        assertEquals(2, batch.size());
        IngestEventPropertyFile ingestedFile1 = batch.get(0).properties().getProperty("file:content");
        assertEquals(Optional.of("id-1"), ingestedFile1.id());
        assertEquals(Optional.of("text/plain"), ingestedFile1.contentType());
        assertEquals(Optional.of("first.txt"), ingestedFile1.name());
        assertEquals(OptionalLong.of(11L), ingestedFile1.size());
        IngestEventPropertyFile ingestedFile2 = batch.get(1).properties().getProperty("file:content");
        assertEquals(Optional.of("id-2"), ingestedFile2.id());
        assertEquals(Optional.of("application/json"), ingestedFile2.contentType());
        assertEquals(Optional.of("second.json"), ingestedFile2.name());
        assertEquals(OptionalLong.of(22L), ingestedFile2.size());
    }

    @Test
    void testIngestWithSourceId() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .sourceId("source-2")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("title", "Test Document")
                               .build();

        service.ingest(event);

        assertEquals(1, httpClient.ingestCalls.size());
        var batch = httpClient.ingestCalls.get(0);
        assertEquals(1, batch.size());
        var ingestedEvent = batch.get(0);
        assertEquals(event, ingestedEvent);
        assertEquals("source-2", ingestedEvent.sourceId().orElseThrow());
    }

    @Test
    void testIngestUploadsBlobsAndSetsId() {
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();
        var fileProperty = IngestEventPropertyFile.builder(blob)
                                                  .contentType("text/plain")
                                                  .name("test.txt")
                                                  .size(12L)
                                                  .build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("file:content", fileProperty)
                               .build();

        httpClient.preSignedUrls.add(new PreSignedUrl("uploaded-id", "https://localhost/upload1"));

        service.ingest(event);

        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals("https://localhost/upload1", httpClient.uploadCalls.get(0).url());
        assertEquals(blob, httpClient.uploadCalls.get(0).blob());

        assertEquals(1, httpClient.ingestCalls.size());
        var ingestedEvent = httpClient.ingestCalls.get(0).get(0);
        IngestEventPropertyFile ingestedFile = ingestedEvent.properties().getProperty("file:content");
        assertEquals(Optional.of("uploaded-id"), ingestedFile.id());
        assertEquals(Optional.of("text/plain"), ingestedFile.contentType());
        assertEquals(Optional.of("test.txt"), ingestedFile.name());
        assertEquals(OptionalLong.of(12L), ingestedFile.size());
        assertTrue(ingestedFile.blob().isEmpty(), "Blob should not be carried over to the ingested event");
    }

    @Test
    void testIngestPropagatesMetadataFromBlob() {
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .contentType("text/markdown")
                              .name("readme.md")
                              .size(42L)
                              .digest("sha256:abc")
                              .build();
        var fileProperty = IngestEventPropertyFile.builder(blob).build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .putProperty("file:content", fileProperty)
                               .build();

        httpClient.preSignedUrls.add(new PreSignedUrl("uploaded-id", "https://localhost/upload1"));

        service.ingest(event);

        assertEquals(1, httpClient.ingestCalls.size());
        IngestEventPropertyFile ingestedFile = httpClient.ingestCalls.get(0)
                                                                     .get(0)
                                                                     .properties()
                                                                     .getProperty("file:content");
        assertEquals(Optional.of("uploaded-id"), ingestedFile.id());
        assertEquals(Optional.of("text/markdown"), ingestedFile.contentType());
        assertEquals(Optional.of("readme.md"), ingestedFile.name());
        assertEquals(OptionalLong.of(42L), ingestedFile.size());
        assertEquals(Optional.of("sha256:abc"), ingestedFile.digest());
    }

    @Test
    void testIngestDropsFilePropertyWhenDigestAlreadyExistsAndNoIdYet() {
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .contentType("text/plain")
                              .digest("sha256:abc123")
                              .build();
        var fileProperty = IngestEventPropertyFile.builder(blob).build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .putProperty("title", "Test Document")
                               .putProperty("file:content", fileProperty)
                               .build();

        httpClient.digestCheckResult = true;

        service.ingest(event);

        assertTrue(httpClient.uploadCalls.isEmpty(), "Upload should not be called when digest already exists");
        assertEquals(1, httpClient.ingestCalls.size());
        var ingestedProperties = httpClient.ingestCalls.get(0).get(0).properties();
        assertNotNull(ingestedProperties.getProperty("title"), "Unrelated properties should be kept");
        assertNull(ingestedProperties.getProperty("file:content"),
                "File property should be dropped: the object already has this content and we have no id to reference it");
    }

    @Test
    void testIngestKeepsFilePropertyWhenDigestAlreadyExistsButIdIsKnown() {
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .contentType("text/plain")
                              .digest("sha256:abc123")
                              .build();
        var fileProperty = IngestEventPropertyFile.builder(blob).id("already-known-id").build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .putProperty("file:content", fileProperty)
                               .build();

        httpClient.digestCheckResult = true;

        service.ingest(event);

        assertTrue(httpClient.checkDigestCalls.isEmpty(),
                "Digest should not be checked when an id is already known: the blob is trusted as already uploaded");
        assertTrue(httpClient.uploadCalls.isEmpty(), "Upload should not be called when digest already exists");
        assertEquals(1, httpClient.ingestCalls.size());
        IngestEventPropertyFile ingestedFile = httpClient.ingestCalls.get(0)
                                                                     .get(0)
                                                                     .properties()
                                                                     .getProperty("file:content");
        assertEquals(Optional.of("already-known-id"), ingestedFile.id());
        assertTrue(ingestedFile.blob().isEmpty(), "Blob should not be carried over to the ingested event");
    }

    @Test
    void testIngestKeepsFilePropertyWhenIdIsKnownEvenWithoutDigest() {
        // A retried event may still carry the blob (without a digest) alongside an id already assigned by a
        // previous, successful attempt: the id must be trusted as-is, without re-uploading the blob.
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .contentType("text/plain")
                              .build();
        var fileProperty = IngestEventPropertyFile.builder(blob).id("already-known-id").build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .putProperty("file:content", fileProperty)
                               .build();

        service.ingest(event);

        assertTrue(httpClient.checkDigestCalls.isEmpty(), "Digest should not be checked when an id is already known");
        assertTrue(httpClient.uploadCalls.isEmpty(), "Upload should not be called when an id is already known");
        assertEquals(1, httpClient.ingestCalls.size());
        IngestEventPropertyFile ingestedFile = httpClient.ingestCalls.get(0)
                                                                     .get(0)
                                                                     .properties()
                                                                     .getProperty("file:content");
        assertEquals(Optional.of("already-known-id"), ingestedFile.id());
        assertTrue(ingestedFile.blob().isEmpty(), "Blob should not be carried over to the ingested event");
    }

    @Test
    void testIngestSkipsUploadForFilePropertyWithoutBlob() {
        var fileProperty = IngestEventPropertyFile.builder().id("existing-id").contentType("text/plain").build();
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("file:content", fileProperty)
                               .build();

        service.ingest(event);

        assertTrue(httpClient.uploadCalls.isEmpty(), "Upload should not be called when no blob is present");
        assertEquals(1, httpClient.ingestCalls.size());
    }

    @Test
    void testUploadBlobIfNeededWithEvent() {
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "doc123").build();
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .digest("sha256:abc123")
                              .build();

        httpClient.digestCheckResult = false;
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://localhost/upload1"));

        service.uploadBlobIfNeeded(event, blob);

        assertEquals(1, httpClient.checkDigestCalls.size());
        assertEquals("source-1", httpClient.checkDigestCalls.get(0).sourceId());
        assertEquals("doc123", httpClient.checkDigestCalls.get(0).objectId());
        assertEquals(1, httpClient.uploadCalls.size());
    }

    @Test
    void testUploadBlobIfNeededWithDigestExists() {
        String sourceId = "source-1";
        String objectId = "doc123";
        String digest = "sha256:abc123";
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .digest(digest)
                              .build();

        httpClient.digestCheckResult = true;

        service.uploadBlobIfNeeded(sourceId, objectId, blob);

        assertEquals(1, httpClient.checkDigestCalls.size());
        assertEquals(sourceId, httpClient.checkDigestCalls.get(0).sourceId());
        assertEquals(objectId, httpClient.checkDigestCalls.get(0).objectId());
        assertEquals(digest, httpClient.checkDigestCalls.get(0).digest());
        assertTrue(httpClient.uploadCalls.isEmpty(), "Upload should not be called when digest exists");
    }

    @Test
    void testUploadBlobIfNeededWithDigestNotExists() {
        String sourceId = "source-1";
        String objectId = "doc123";
        String digest = "sha256:xyz789";
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes()))
                              .digest(digest)
                              .build();

        httpClient.digestCheckResult = false;
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://localhost/upload1"));

        service.uploadBlobIfNeeded(sourceId, objectId, blob);

        assertEquals(1, httpClient.checkDigestCalls.size());
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals("https://localhost/upload1", httpClient.uploadCalls.get(0).url());
        assertEquals(blob, httpClient.uploadCalls.get(0).blob());
    }

    @Test
    void testUploadBlobIfNeededWithoutDigest() {
        String sourceId = "source-1";
        String objectId = "doc123";
        CICBlob blob = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();

        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://localhost/upload1"));

        service.uploadBlobIfNeeded(sourceId, objectId, blob);

        assertTrue(httpClient.checkDigestCalls.isEmpty(), "Digest check should not be called without digest");
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals(1, httpClient.retrievePreSignedUrlsCalls);
    }

    @Test
    void testGetPreSignedUrlCaching() {
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://localhost/upload1"));
        httpClient.preSignedUrls.add(new PreSignedUrl("url-2", "https://localhost/upload2"));

        CICBlob blob1 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();
        CICBlob blob2 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();

        service.uploadBlobIfNeeded("source-1", "doc1", blob1);
        service.uploadBlobIfNeeded("source-1", "doc2", blob2);

        assertEquals(1, httpClient.retrievePreSignedUrlsCalls, "Should only fetch pre-signed URLs once");
        assertEquals(2, httpClient.uploadCalls.size());
        assertEquals("https://localhost/upload1", httpClient.uploadCalls.get(0).url());
        assertEquals("https://localhost/upload2", httpClient.uploadCalls.get(1).url());
    }

    @Test
    void testGetPreSignedUrlRetrievesMoreWhenExhausted() {
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://localhost/upload1"));

        CICBlob blob1 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();
        CICBlob blob2 = CICBlob.builder(() -> new ByteArrayInputStream("test content".getBytes())).build();

        service.uploadBlobIfNeeded("source-1", "doc1", blob1);

        httpClient.preSignedUrls.add(new PreSignedUrl("url-2", "https://localhost/upload2"));

        service.uploadBlobIfNeeded("source-1", "doc2", blob2);

        assertEquals(2, httpClient.retrievePreSignedUrlsCalls, "Should fetch pre-signed URLs again when exhausted");
    }

    private record CheckDigestCall(String sourceId, String objectId, String digest) {
    }

    private record UploadCall(String url, CICBlob blob) {
    }

    private static class TestIngestHttpClient extends IngestHttpClient {

        List<IngestEvent.Batch> ingestCalls = new ArrayList<>();

        List<CheckDigestCall> checkDigestCalls = new ArrayList<>();

        List<UploadCall> uploadCalls = new ArrayList<>();

        int retrievePreSignedUrlsCalls = 0;

        boolean digestCheckResult = false;

        List<PreSignedUrl> preSignedUrls = new ArrayList<>();

        public TestIngestHttpClient() {
            super(IngestHttpClient.from("https://localhost",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret"))
                                  .sourceId("source-1"));
        }

        @Override
        public void ingest(IngestEvent.Batch batch) {
            ingestCalls.add(batch);
        }

        @Override
        public boolean checkDigest(String sourceId, String objectId, String digest) {
            // TODO maybe mock with a real HTTP server, sourceId resolution is source duplicated
            checkDigestCalls.add(new CheckDigestCall(sourceId == null ? this.sourceId : sourceId, objectId, digest));
            return digestCheckResult;
        }

        @Override
        public void upload(String preSignedUrl, CICBlob blob) {
            uploadCalls.add(new UploadCall(preSignedUrl, blob));
        }

        @Override
        public List<PreSignedUrl> retrievePreSignedUrls() {
            retrievePreSignedUrlsCalls++;
            return new ArrayList<>(preSignedUrls);
        }
    }
}
