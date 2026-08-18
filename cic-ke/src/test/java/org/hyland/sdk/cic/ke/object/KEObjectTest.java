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
package org.hyland.sdk.cic.ke.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class KEObjectTest {

    // --- Action enum ---

    @Test
    void testActionValues() {
        assertEquals("image-classification", Action.IMAGE_CLASSIFICATION.value());
        assertEquals("image-description", Action.IMAGE_DESCRIPTION.value());
        assertEquals("image-embeddings", Action.IMAGE_EMBEDDINGS.value());
        assertEquals("image-metadata-generation", Action.IMAGE_METADATA_GENERATION.value());
        assertEquals("named-entity-recognition-image", Action.NAMED_ENTITY_RECOGNITION_IMAGE.value());
        assertEquals("named-entity-recognition-text", Action.NAMED_ENTITY_RECOGNITION_TEXT.value());
        assertEquals("text-classification", Action.TEXT_CLASSIFICATION.value());
        assertEquals("text-embeddings", Action.TEXT_EMBEDDINGS.value());
        assertEquals("text-metadata-generation", Action.TEXT_METADATA_GENERATION.value());
        assertEquals("text-summarization", Action.TEXT_SUMMARIZATION.value());
    }

    @Test
    void testActionEnumCount() {
        assertEquals(10, Action.values().length);
    }

    // --- ProcessRequest builder ---

    @Test
    void testProcessRequestBuilderWithAllFields() {
        var request = ProcessRequest.builder()
                                    .objectKey("contents/file.pdf")
                                    .action(Action.TEXT_SUMMARIZATION)
                                    .action("image-description")
                                    .addClass("invoice")
                                    .addSimilarMetadata(Map.of("title", "Sample"))
                                    .maxWordCount(150)
                                    .instructions("Be concise")
                                    .extraJsonPayload("{\"custom\":true}")
                                    .build();

        assertEquals(1, request.objectKeys().size());
        assertEquals("contents/file.pdf", request.objectKeys().get(0).path());
        assertEquals(2, request.actions().size());
        assertEquals("text-summarization", request.actions().get(0));
        assertEquals("image-description", request.actions().get(1));
        assertEquals(List.of("invoice"), request.classes());
        assertEquals(1, request.kSimilarMetadata().size());
        assertEquals(150, request.maxWordCount());
        assertEquals("Be concise", request.instructions());
        assertEquals("{\"custom\":true}", request.extraJsonPayload());
    }

    @Test
    void testProcessRequestBuilderEmptyObjectKeysThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ProcessRequest.builder().action("text-summarization").build());
    }

    @Test
    void testProcessRequestNullOptionalFields() {
        var request = ProcessRequest.builder().objectKey("path").action("action").build();

        assertNull(request.classes());
        assertNull(request.kSimilarMetadata());
        assertNull(request.maxWordCount());
        assertNull(request.instructions());
        assertNull(request.extraJsonPayload());
    }

    @Test
    void testProcessRequestEquality() {
        var r1 = ProcessRequest.builder().objectKey("a").action("b").maxWordCount(10).instructions("x").build();
        var r2 = ProcessRequest.builder().objectKey("a").action("b").maxWordCount(10).instructions("x").build();
        var r3 = ProcessRequest.builder().objectKey("a").action("b").maxWordCount(20).build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    @Test
    void testProcessRequestMultipleObjectKeys() {
        var request = ProcessRequest.builder()
                                    .objectKey("path1")
                                    .objectKey("path2")
                                    .objectKeys(List.of(new ObjectKeyPath("path3")))
                                    .action("action")
                                    .build();

        assertEquals(3, request.objectKeys().size());
    }

    // --- ProcessingOptions builder ---

    @Test
    void testProcessingOptionsBuilderWithAllFields() {
        var options = ProcessingOptions.builder()
                                       .normalization(n -> n.quotations(true).dashes(false))
                                       .chunking(true)
                                       .chunkingStrategy("context")
                                       .chunkSize(2000)
                                       .embedding(true)
                                       .embeddingsModel("cohere-v3")
                                       .jsonSchema("my-schema")
                                       .pii(p -> p.mode("redaction").entityRedaction(true))
                                       .build();

        assertNotNull(options.normalization());
        assertEquals(true, options.normalization().quotations());
        assertEquals(false, options.normalization().dashes());
        assertEquals(true, options.chunking());
        assertEquals("context", options.chunkingStrategy());
        assertEquals(2000, options.chunkSize());
        assertEquals(true, options.embedding());
        assertEquals("cohere-v3", options.embeddingsModel());
        assertEquals("my-schema", options.jsonSchema());
        assertNotNull(options.pii());
        assertEquals("redaction", options.pii().mode());
        assertEquals(true, options.pii().entityRedaction());
    }

    @Test
    void testProcessingOptionsJsonSchemaBoolean() {
        var options = ProcessingOptions.builder().jsonSchema(true).build();

        assertEquals(true, options.jsonSchema());
    }

    @Test
    void testProcessingOptionsEquality() {
        var o1 = ProcessingOptions.builder().chunking(true).chunkSize(1000).build();
        var o2 = ProcessingOptions.builder().chunking(true).chunkSize(1000).build();
        var o3 = ProcessingOptions.builder().chunking(false).build();

        assertEquals(o1, o2);
        assertEquals(o1.hashCode(), o2.hashCode());
        assertNotEquals(o1, o3);
    }

    // --- ConfigRule builder ---

    @Test
    void testConfigRuleBuilder() {
        var rule = ConfigRule.builder()
                             .id("r1")
                             .name("PDF Rule")
                             .addCondition("content_type", "application/pdf")
                             .addCondition("size", "large")
                             .config(ProcessingOptions.builder().chunking(true).build())
                             .build();

        assertEquals("r1", rule.id());
        assertEquals("PDF Rule", rule.name());
        assertEquals(2, rule.conditions().size());
        assertEquals("content_type", rule.conditions().get(0).field());
        assertEquals("application/pdf", rule.conditions().get(0).value());
        assertNotNull(rule.config());
        assertEquals(true, rule.config().chunking());
    }

    @Test
    void testConfigRuleEquality() {
        var r1 = ConfigRule.builder().id("r1").name("Rule").build();
        var r2 = ConfigRule.builder().id("r1").name("Rule").build();
        var r3 = ConfigRule.builder().id("r2").name("Other").build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    // --- NormalizationOptions ---

    @Test
    void testNormalizationOptionsBuilder() {
        var opts = NormalizationOptions.builder().quotations(true).dashes(false).build();

        assertEquals(true, opts.quotations());
        assertEquals(false, opts.dashes());
    }

    // --- PiiOptions ---

    @Test
    void testPiiOptionsBuilder() {
        var opts = PiiOptions.builder().mode("redaction").entityRedaction(true).build();

        assertEquals("redaction", opts.mode());
        assertEquals(true, opts.entityRedaction());
    }

    // --- RuleTestRequest builder ---

    @Test
    void testRuleTestRequestBuilder() {
        var request = RuleTestRequest.builder()
                                     .property("content_type", "application/pdf")
                                     .property("size", "1024")
                                     .build();

        assertEquals(2, request.properties().size());
        assertEquals("application/pdf", request.properties().get("content_type"));
        assertEquals("1024", request.properties().get("size"));
    }

    @Test
    void testRuleTestRequestEquality() {
        var r1 = RuleTestRequest.builder().property("a", "b").build();
        var r2 = RuleTestRequest.builder().property("a", "b").build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    // --- JobStatus ---

    @Test
    void testJobStatusIsDone() {
        assertTrue(new JobStatus("j", "Done").isDone());
        assertTrue(new JobStatus("j", "done").isDone());
        assertTrue(new JobStatus("j", "DONE").isDone());
        assertFalse(new JobStatus("j", "Processing").isDone());
        assertFalse(new JobStatus("j", "Wait For Upload").isDone());
    }

    // --- EnrichmentResult ---

    @Test
    void testEnrichmentResultConvenienceMethods() {
        var success = new EnrichmentResult("id", "ts", List.of(), "SUCCESS", false);
        assertTrue(success.isSuccess());
        assertTrue(success.isComplete());

        var inProgress = new EnrichmentResult("id", "ts", List.of(), "PROCESSING", true);
        assertFalse(inProgress.isSuccess());
        assertFalse(inProgress.isComplete());

        var caseInsensitive = new EnrichmentResult("id", "ts", List.of(), "success", false);
        assertTrue(caseInsensitive.isSuccess());
    }

    // --- Simple records ---

    @Test
    void testActionResult() {
        var success = new ActionResult<>(true, "result-value", null);
        assertTrue(success.isSuccess());
        assertEquals("result-value", success.result());
        assertNull(success.error());

        var failure = new ActionResult<>(false, null, "error-msg");
        assertFalse(failure.isSuccess());
        assertEquals("error-msg", failure.error());
    }

    @Test
    void testObjectKeyPath() {
        var path = new ObjectKeyPath("contents/file.pdf");
        assertEquals("contents/file.pdf", path.path());
    }

    @Test
    void testPresignedUrl() {
        var url = new PresignedUrl("https://upload.url", "contents/file.pdf");
        assertEquals("https://upload.url", url.presignedUrl());
        assertEquals("contents/file.pdf", url.objectKey());
    }

    @Test
    void testPresignResponse() {
        var response = new PresignResponse("job-1", "https://put.url", "https://get.url");
        assertEquals("job-1", response.jobId());
        assertEquals("https://put.url", response.putUrl());
        assertEquals("https://get.url", response.getUrl());
    }

    @Test
    void testProcessResponse() {
        var response = new ProcessResponse("proc-id");
        assertEquals("proc-id", response.processingId());
    }

    @Test
    void testRuleCondition() {
        var condition = new RuleCondition("content_type", "application/pdf");
        assertEquals("content_type", condition.field());
        assertEquals("application/pdf", condition.value());
    }

    @Test
    void testRuleTestResponse() {
        var rule = ConfigRule.builder().name("Rule").build();
        var config = ProcessingOptions.builder().chunking(true).build();
        var response = new RuleTestResponse(rule, config);
        assertEquals("Rule", response.matchedRule().name());
        assertEquals(true, response.effectiveConfig().chunking());
    }

    @Test
    void testConfigDefaults() {
        var opts = ProcessingOptions.builder().chunking(true).build();
        var defaults = new ConfigDefaults(opts);
        assertEquals(true, defaults.options().chunking());
    }

    @Test
    void testConfigOptions() {
        var defaults = ProcessingOptions.builder().embedding(true).build();
        var rule = ConfigRule.builder().name("Rule").build();
        var config = new ConfigOptions(defaults, List.of(rule));
        assertEquals(true, config.defaults().embedding());
        assertEquals(1, config.rules().size());
    }

    @Test
    void testCurationResult() {
        var result = new CurationResult("# Markdown", List.of(Map.of("chunk", "data")), List.of(), Map.of("key", "v"));
        assertEquals("# Markdown", result.markdownOutput());
        assertEquals(1, result.chunksWithEmbeddings().size());
        assertTrue(result.piiMatches().isEmpty());
        assertEquals("v", result.rawResult().get("key"));
    }

    @Test
    void testEmbeddingModel() {
        var model = new EmbeddingModel("id-1", "Model Name");
        assertEquals("id-1", model.id());
        assertEquals("Model Name", model.name());
    }

    @Test
    void testEnrichmentResultEntry() {
        var textSummary = new ActionResult<>(true, "Summary", null);
        var entry = new EnrichmentResultEntry("key", null, null, null, textSummary, null, null, null, null, null, null,
                null);
        assertEquals("key", entry.objectKey());
        assertNotNull(entry.textSummary());
        assertEquals("Summary", entry.textSummary().result());
        assertNull(entry.imageDescription());
        assertNull(entry.generalProcessingErrors());
    }
}
