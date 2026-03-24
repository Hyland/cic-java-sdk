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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.ingest;

import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

/**
 * @since 1.0.0
 */
public class IngestService {

    protected final IngestHttpClient httpClient;

    protected final Deque<PreSignedUrl> preSignedUrls = new ConcurrentLinkedDeque<>();

    public IngestService(IngestHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<PreSignedUrl> uploadBlobIfNeeded(IngestEvent event, CICBlob blob) {
        return uploadBlobIfNeeded(event.sourceId(), event.objectId(), blob);
    }

    public Optional<PreSignedUrl> uploadBlobIfNeeded(String sourceId, String objectId, CICBlob blob) {
        if (blob.getDigest().isPresent() && httpClient.checkDigest(sourceId, objectId, blob.getDigest().get())) {
            // document already have the blob, no need to upload it again
            return Optional.empty();
        }
        var preSignedUrl = getPreSignedUrl();
        httpClient.upload(preSignedUrl.url(), blob);
        return Optional.of(preSignedUrl);
    }

    public void ingest(IngestEvent event) {
        httpClient.ingest(event);
    }

    protected PreSignedUrl getPreSignedUrl() {
        PreSignedUrl preSignedUrl = preSignedUrls.pollFirst();
        if (preSignedUrl == null) {
            synchronized (this) {
                if (preSignedUrls.isEmpty()) {
                    preSignedUrls.addAll(httpClient.retrievePreSignedUrls());
                }
                preSignedUrl = preSignedUrls.pollFirst();
            }
        }
        return preSignedUrl;
    }
}
