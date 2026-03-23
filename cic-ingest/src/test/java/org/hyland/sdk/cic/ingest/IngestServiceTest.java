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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
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
        var event = IngestEvent.builder(IngestEvent.Type.CREATE, "source-1", "doc123")
                               .date(Instant.ofEpochMilli(1609459200000L))
                               .putProperty("title", "Test Document")
                               .build();

        service.ingest(event);

        assertEquals(1, httpClient.ingestCalls.size());
        assertEquals(event, httpClient.ingestCalls.get(0));
    }

    @Test
    void testUploadBlobIfNeededWithDigestExists() {
        String sourceId = "source-1";
        String objectId = "doc123";
        String digest = "sha256:abc123";
        CICBlob blob = createTestBlob(digest);

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
        CICBlob blob = createTestBlob(digest);

        httpClient.digestCheckResult = false;
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://acme.com/upload1"));

        service.uploadBlobIfNeeded(sourceId, objectId, blob);

        assertEquals(1, httpClient.checkDigestCalls.size());
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals("https://acme.com/upload1", httpClient.uploadCalls.get(0).url());
        assertEquals(blob, httpClient.uploadCalls.get(0).blob());
    }

    @Test
    void testUploadBlobIfNeededWithoutDigest() {
        String sourceId = "source-1";
        String objectId = "doc123";
        CICBlob blob = createTestBlob(null);

        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://acme.com/upload1"));

        service.uploadBlobIfNeeded(sourceId, objectId, blob);

        assertTrue(httpClient.checkDigestCalls.isEmpty(), "Digest check should not be called without digest");
        assertEquals(1, httpClient.uploadCalls.size());
        assertEquals(1, httpClient.retrievePreSignedUrlsCalls);
    }

    @Test
    void testGetPreSignedUrlCaching() {
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://acme.com/upload1"));
        httpClient.preSignedUrls.add(new PreSignedUrl("url-2", "https://acme.com/upload2"));

        CICBlob blob1 = createTestBlob(null);
        CICBlob blob2 = createTestBlob(null);

        service.uploadBlobIfNeeded("source-1", "doc1", blob1);
        service.uploadBlobIfNeeded("source-1", "doc2", blob2);

        assertEquals(1, httpClient.retrievePreSignedUrlsCalls, "Should only fetch pre-signed URLs once");
        assertEquals(2, httpClient.uploadCalls.size());
        assertEquals("https://acme.com/upload1", httpClient.uploadCalls.get(0).url());
        assertEquals("https://acme.com/upload2", httpClient.uploadCalls.get(1).url());
    }

    @Test
    void testGetPreSignedUrlRetrievesMoreWhenExhausted() {
        httpClient.preSignedUrls.add(new PreSignedUrl("url-1", "https://acme.com/upload1"));

        CICBlob blob1 = createTestBlob(null);
        CICBlob blob2 = createTestBlob(null);

        service.uploadBlobIfNeeded("source-1", "doc1", blob1);

        httpClient.preSignedUrls.add(new PreSignedUrl("url-2", "https://acme.com/upload2"));

        service.uploadBlobIfNeeded("source-1", "doc2", blob2);

        assertEquals(2, httpClient.retrievePreSignedUrlsCalls, "Should fetch pre-signed URLs again when exhausted");
    }

    private CICBlob createTestBlob(String digest) {
        return new CICBlob() {
            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream("test content".getBytes());
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.ofNullable(digest);
            }
        };
    }

    private record CheckDigestCall(String sourceId, String objectId, String digest) {
    }

    private record UploadCall(String url, CICBlob blob) {
    }

    private static class TestIngestHttpClient extends IngestHttpClient {

        List<IngestEvent> ingestCalls = new ArrayList<>();

        List<CheckDigestCall> checkDigestCalls = new ArrayList<>();

        List<UploadCall> uploadCalls = new ArrayList<>();

        int retrievePreSignedUrlsCalls = 0;

        boolean digestCheckResult = false;

        List<PreSignedUrl> preSignedUrls = new ArrayList<>();

        public TestIngestHttpClient() {
            super(IngestHttpClient.from("https://test.example.com",
                    AuthenticationHttpClient.from().clientId("test-client-id").clientSecret("test-client-secret")));
        }

        @Override
        public void ingest(IngestEvent event) {
            ingestCalls.add(event);
        }

        @Override
        public boolean checkDigest(String sourceId, String objectId, String digest) {
            checkDigestCalls.add(new CheckDigestCall(sourceId, objectId, digest));
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
