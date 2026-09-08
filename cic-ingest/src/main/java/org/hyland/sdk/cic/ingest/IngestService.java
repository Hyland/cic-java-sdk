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
import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyFile;
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

    /**
     * Uploads the given blob if needed, ie: blob does not have a digest or is the same as remote.
     * <p>
     * The {@code sourceId} from given {@link IngestEvent} will be used if not null, otherwise the configured
     * {@link IngestHttpClient.Builder#sourceId(String)} will be used.
     */
    public Optional<PreSignedUrl> uploadBlobIfNeeded(IngestEvent event, CICBlob blob) {
        return uploadBlobIfNeeded(event.sourceId().orElse(null), event.objectId(), blob);
    }

    /**
     * Uploads the given blob if needed, ie: blob does not have a digest or is the same as remote.
     * <p>
     * Usage of this method is permitted when the underlying {@link IngestHttpClient} is built with
     * {@link IngestHttpClient.Builder#sourceId(String)}.
     */
    public Optional<PreSignedUrl> uploadBlobIfNeeded(String objectId, CICBlob blob) {
        return uploadBlobIfNeeded(null, objectId, blob);
    }

    /**
     * Uploads the given blob if needed, ie: blob does not have a digest or is the same as remote.
     * <p>
     * The given {@code sourceId} will be used if not null, otherwise the configured
     * {@link IngestHttpClient.Builder#sourceId(String)} will be used.
     */
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
        ingest(IngestEvent.Batch.of(event));
    }

    /**
     * Ingests a batch of events, automatically uploading any blob referenced by an {@link IngestEventPropertyFile}
     * property beforehand.
     *
     * @since 1.1.0
     */
    public void ingest(IngestEvent.Batch batch) {
        httpClient.ingest(
                batch.stream().map(this::uploadBlobsIfNeeded).collect(Collectors.toCollection(IngestEvent.Batch::new)));
    }

    /**
     * Walks the given event's properties, uploading any blob referenced by an {@link IngestEventPropertyFile},
     * replacing it with an {@code id}-bearing property ready to be sent to the wire.
     *
     * @since 1.1.0
     */
    protected IngestEvent uploadBlobsIfNeeded(IngestEvent event) {
        return event.toBuilder().replaceProperties((key, property) -> {
            if (property instanceof IngestEventPropertyFile propertyFile && propertyFile.blob().isPresent()) {
                var propertyFileBuilder = propertyFile.toBuilder();
                uploadBlobIfNeeded(event, propertyFile.blob().get()).map(PreSignedUrl::id)
                                                                    .ifPresent(propertyFileBuilder::id);
                return propertyFileBuilder.build();
            } else {
                return property;
            }
        }).build();
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
