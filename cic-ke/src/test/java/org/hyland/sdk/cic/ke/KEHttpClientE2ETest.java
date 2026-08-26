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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICServiceException;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ActionConfig;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.ProcessRequest;

/**
 * End-to-end tests for {@link KEHttpClient} against the staging CIC Context API.
 * <p>
 * Requires the following environment variables:
 * <ul>
 * <li>{@code CIC_BASE_URL} - Context API base URL</li>
 * <li>{@code CIC_AUTH_URL} - OAuth2 token endpoint base URL</li>
 * <li>{@code CIC_CLIENT_ID} - OAuth2 client ID</li>
 * <li>{@code CIC_CLIENT_SECRET} - OAuth2 client secret</li>
 * </ul>
 * <p>
 * Sample files are loaded from {@code src/test/resources/e2e/}.
 *
 * @since 1.0.0
 */
@Tag("e2e")
class KEHttpClientE2ETest {

    private static final String SAMPLE_DOCUMENT = "/e2e/sample-document.txt";

    private static final String SAMPLE_SHORT = "/e2e/sample-short.txt";

    private static final Set<String> STANDARD_PUBLIC_ACTIONS = Set.of("imageClassification", "imageDescription",
            "imageEmbeddings", "imageMetadataGeneration", "namedEntityRecognitionImage", "namedEntityRecognitionText",
            "textClassification", "textEmbeddings", "textMetadataGeneration", "textSummarization");

    private static KEHttpClient client;

    private static KEService service;

    @BeforeAll
    static void setUp() {
        assumeTrue(System.getenv("CIC_CLIENT_ID") != null, "Skipping E2E: CIC_CLIENT_ID not set");
        assumeTrue(System.getenv("CIC_CLIENT_SECRET") != null, "Skipping E2E: CIC_CLIENT_SECRET not set");
        assumeTrue(System.getenv("CIC_BASE_URL") != null, "Skipping E2E: CIC_BASE_URL not set");
        assumeTrue(System.getenv("CIC_AUTH_URL") != null, "Skipping E2E: CIC_AUTH_URL not set");

        client = KEHttpClient.from(System.getenv("CIC_BASE_URL"),
                AuthenticationHttpClient.from(System.getenv("CIC_AUTH_URL"))
                                        .clientId(System.getenv("CIC_CLIENT_ID"))
                                        .clientSecret(System.getenv("CIC_CLIENT_SECRET")))
                             .build();

        service = new KEService(client);
        service.setPollSettings(30, 5000);
    }

    // -------------------------------------------------------
    // Basic connectivity tests
    // -------------------------------------------------------

    @Test
    void healthCheckReturnsTrue() {
        assertTrue(client.isHealthy());
    }

    @Test
    void getActionDescriptorsMatchPublicContract() {
        var descriptors = client.getActionDescriptors();

        assertNotNull(descriptors);
        assertFalse(descriptors.isEmpty());

        var names = descriptors.stream().map(d -> d.name()).toList();
        assertTrue(names.containsAll(STANDARD_PUBLIC_ACTIONS),
                () -> "Missing standard Context API actions. Returned actions: " + names);
    }

    // -------------------------------------------------------
    // Single-action enrichment tests
    // -------------------------------------------------------

    @Test
    void fullEnrichmentWithSampleDocument() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        assertNotNull(presigned.presignedUrl());
        assertNotNull(presigned.objectKey());

        client.upload(presigned.presignedUrl(), blob);

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.TEXT_SUMMARIZATION)
                                           .build();

        var processingId = client.process(processRequest);
        assertNotNull(processingId);

        EnrichmentResult result = pollForResult(processingId, 30, 5000);
        assertNotNull(result, "Enrichment did not complete within timeout");
        assertNotNull(result.results());
        assertFalse(result.results().isEmpty());
        assertTrue(result.isSuccess());
    }

    @Test
    void enrichmentWithNerAction() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_SHORT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.NAMED_ENTITY_RECOGNITION_TEXT)
                                           .build();

        var processingId = client.process(processRequest);
        assertNotNull(processingId);

        EnrichmentResult result = pollForResult(processingId, 30, 5000);
        assertNotNull(result, "NER enrichment did not complete within timeout");
        assertFalse(result.results().isEmpty());
    }

    @Test
    void enrichmentWithTextEmbeddings() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.TEXT_EMBEDDINGS)
                                           .build();

        var processingId = client.process(processRequest);
        EnrichmentResult result = pollForResult(processingId, 30, 5000);

        assertNotNull(result, "Embeddings enrichment did not complete within timeout");
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.textEmbeddings());
        assertTrue(entry.textEmbeddings().isSuccess());
        assertNotNull(entry.textEmbeddings().result());
        assertFalse(entry.textEmbeddings().result().isEmpty());
        assertFalse(entry.textEmbeddings().result().get(0).isEmpty());
    }

    @Test
    void enrichmentWithTextClassification() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var classificationConfig = ActionConfig.builder()
                                               .classes(List.of("healthcare", "technology", "finance", "education"))
                                               .build();

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.TEXT_CLASSIFICATION, classificationConfig)
                                           .build();

        var processingId = client.process(processRequest);
        EnrichmentResult result = pollForResult(processingId, 30, 5000);

        assertNotNull(result, "Classification enrichment did not complete within timeout");
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.textClassification());
        assertTrue(entry.textClassification().isSuccess());
        assertNotNull(entry.textClassification().result());
        assertFalse(entry.textClassification().result().isBlank());
    }

    @Test
    void enrichmentWithTextMetadataGeneration() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var metadataConfig = ActionConfig.builder()
                                         .addSimilarMetadata(Map.of("title", "AI in Healthcare", "topic", "medical"))
                                         .build();

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.TEXT_METADATA_GENERATION, metadataConfig)
                                           .build();

        var processingId = client.process(processRequest);
        EnrichmentResult result = pollForResult(processingId, 30, 5000);

        assertNotNull(result, "Metadata generation did not complete within timeout");
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.textMetadata());
        assertTrue(entry.textMetadata().isSuccess());
        assertNotNull(entry.textMetadata().result());
        assertFalse(entry.textMetadata().result().isEmpty());
    }

    // -------------------------------------------------------
    // Multi-action enrichment test
    // -------------------------------------------------------

    @Test
    void multiActionEnrichment() throws InterruptedException {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.TEXT_SUMMARIZATION)
                                           .action(Action.NAMED_ENTITY_RECOGNITION_TEXT)
                                           .build();

        var processingId = client.process(processRequest);
        EnrichmentResult result = pollForResult(processingId, 30, 5000);

        assertNotNull(result, "Multi-action enrichment did not complete within timeout");
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.textSummary(), "Expected textSummary in multi-action result");
        assertTrue(entry.textSummary().isSuccess());
        assertNotNull(entry.textSummary().result());

        assertNotNull(entry.namedEntityText(), "Expected namedEntityText in multi-action result");
        assertTrue(entry.namedEntityText().isSuccess());
        assertNotNull(entry.namedEntityText().result());
        assertFalse(entry.namedEntityText().result().isEmpty());
    }

    // -------------------------------------------------------
    // Pretrained classification (conditional)
    // -------------------------------------------------------

    @Test
    void enrichmentWithPretrainedClassification() throws InterruptedException {
        var descriptors = client.getActionDescriptors();
        var pretrainedDescriptor = descriptors.stream()
                                              .filter(d -> "pretrainedClassification".equals(d.name()))
                                              .findFirst()
                                              .orElse(null);
        assumeTrue(pretrainedDescriptor != null,
                "Skipping: pretrainedClassification not available on this environment");
        assumeTrue(
                pretrainedDescriptor.availableCategories() != null
                        && !pretrainedDescriptor.availableCategories().isEmpty(),
                "Skipping: no categories available for pretrainedClassification");
        assumeTrue(pretrainedDescriptor.availableModels() != null && !pretrainedDescriptor.availableModels().isEmpty(),
                "Skipping: no models available for pretrainedClassification");

        String category = pretrainedDescriptor.availableCategories().get(0);
        String model = pretrainedDescriptor.availableModels().get(0);

        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");
        var presigned = client.getPresignedUrl("text/plain");
        client.upload(presigned.presignedUrl(), blob);

        var config = ActionConfig.builder().category(category).model(model).build();
        var processRequest = ProcessRequest.builder()
                                           .objectPath(presigned.objectKey())
                                           .action(Action.PRETRAINED_CLASSIFICATION, config)
                                           .build();

        var processingId = client.process(processRequest);
        EnrichmentResult result = pollForResult(processingId, 30, 5000);

        assertNotNull(result, "Pretrained classification did not complete within timeout");
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.pretrainedClassification());
        assertTrue(entry.pretrainedClassification().isSuccess());
        assertNotNull(entry.pretrainedClassification().result());
    }

    // -------------------------------------------------------
    // Error handling tests
    // -------------------------------------------------------

    @Test
    void getResultsWithInvalidProcessingIdThrowsServiceException() {
        String fakeId = UUID.randomUUID().toString();

        var exception = assertThrows(CICServiceException.class, () -> client.getResults(fakeId));
        assertTrue(exception.statusCode() >= 400, "Expected 4xx status code but got: " + exception.statusCode());
    }

    // -------------------------------------------------------
    // KEService high-level workflow test
    // -------------------------------------------------------

    @Test
    void keServiceEnrichWorkflow() {
        var blob = loadResourceBlob(SAMPLE_DOCUMENT, "text/plain");

        EnrichmentResult result = service.enrich(blob, List.of(Action.TEXT_SUMMARIZATION));

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertFalse(result.results().isEmpty());

        var entry = result.results().get(0);
        assertNotNull(entry.textSummary());
        assertTrue(entry.textSummary().isSuccess());
        assertNotNull(entry.textSummary().result());
        assertFalse(entry.textSummary().result().isBlank());
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private EnrichmentResult pollForResult(String processingId, int maxAttempts, long intervalMs)
            throws InterruptedException {
        for (int i = 0; i < maxAttempts; i++) {
            var result = client.getResultsIfReady(processingId);
            if (result != null) {
                return result;
            }
            Thread.sleep(intervalMs);
        }
        return null;
    }

    private static CICBlob loadResourceBlob(String resourcePath, String contentType) {
        return new CICBlob() {

            @Override
            public InputStream getInputStream() {
                InputStream is = KEHttpClientE2ETest.class.getResourceAsStream(resourcePath);
                if (is == null) {
                    throw new IllegalStateException("Test resource not found: " + resourcePath);
                }
                return is;
            }

            @Override
            public Optional<String> getDigest() {
                return Optional.empty();
            }

            @Override
            public Optional<String> getContentType() {
                return Optional.of(contentType);
            }
        };
    }
}
