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
        assertEquals("imageClassification", Action.IMAGE_CLASSIFICATION.value());
        assertEquals("imageDescription", Action.IMAGE_DESCRIPTION.value());
        assertEquals("imageEmbeddings", Action.IMAGE_EMBEDDINGS.value());
        assertEquals("imageMetadataGeneration", Action.IMAGE_METADATA_GENERATION.value());
        assertEquals("namedEntityRecognitionImage", Action.NAMED_ENTITY_RECOGNITION_IMAGE.value());
        assertEquals("namedEntityRecognitionText", Action.NAMED_ENTITY_RECOGNITION_TEXT.value());
        assertEquals("textClassification", Action.TEXT_CLASSIFICATION.value());
        assertEquals("textEmbeddings", Action.TEXT_EMBEDDINGS.value());
        assertEquals("textMetadataGeneration", Action.TEXT_METADATA_GENERATION.value());
        assertEquals("textSummarization", Action.TEXT_SUMMARIZATION.value());
    }

    @Test
    void testActionEnumCount() {
        assertEquals(10, Action.values().length);
    }

    // --- ActionConfig builder ---

    @Test
    void testActionConfigEmpty() {
        var config = ActionConfig.empty();
        assertNull(config.classes());
        assertNull(config.maxWordCount());
        assertNull(config.kSimilarMetadata());
        assertNull(config.instructions());
    }

    @Test
    void testActionConfigBuilderWithAllFields() {
        var config = ActionConfig.builder()
                                 .classes(List.of("invoice", "contract"))
                                 .maxWordCount(200)
                                 .addSimilarMetadata(Map.of("title", "Sample"))
                                 .instructions(Map.of("context", "legal documents"))
                                 .build();

        assertEquals(List.of("invoice", "contract"), config.classes());
        assertEquals(200, config.maxWordCount());
        assertEquals(1, config.kSimilarMetadata().size());
        assertEquals("legal documents", config.instructions().get("context"));
    }

    @Test
    void testActionConfigAddClass() {
        var config = ActionConfig.builder().addClass("a").addClass("b").build();
        assertEquals(List.of("a", "b"), config.classes());
    }

    @Test
    void testActionConfigInstruction() {
        var config = ActionConfig.builder().instruction("tone", "professional").instruction("focus", "summary").build();
        assertEquals("professional", config.instructions().get("tone"));
        assertEquals("summary", config.instructions().get("focus"));
    }

    @Test
    void testActionConfigEquality() {
        var c1 = ActionConfig.builder().maxWordCount(100).build();
        var c2 = ActionConfig.builder().maxWordCount(100).build();
        var c3 = ActionConfig.builder().maxWordCount(200).build();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }

    // --- ProcessRequest builder (v2) ---

    @Test
    void testProcessRequestBuilderWithAllFields() {
        var request = ProcessRequest.builder()
                                    .objectKey("contents/file.pdf")
                                    .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(150))
                                    .action(Action.TEXT_CLASSIFICATION,
                                            cfg -> cfg.classes(List.of("invoice")).instruction("context", "legal"))
                                    .build();

        assertEquals(ProcessRequest.VERSION_V2, request.version());
        assertEquals(1, request.objectKeys().size());
        assertEquals("contents/file.pdf", request.objectKeys().get(0).path());
        assertEquals(2, request.actions().size());
        assertNotNull(request.actions().get("textSummarization"));
        assertEquals(150, request.actions().get("textSummarization").maxWordCount());
        assertNotNull(request.actions().get("textClassification"));
        assertEquals(List.of("invoice"), request.actions().get("textClassification").classes());
        assertEquals("legal", request.actions().get("textClassification").instructions().get("context"));
    }

    @Test
    void testProcessRequestBuilderEmptyObjectKeysThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> ProcessRequest.builder().action(Action.TEXT_SUMMARIZATION).build());
    }

    @Test
    void testProcessRequestNullOptionalFields() {
        var request = ProcessRequest.builder().objectKey("path").action(Action.TEXT_EMBEDDINGS).build();

        assertEquals(ProcessRequest.VERSION_V2, request.version());
        var config = request.actions().get("textEmbeddings");
        assertNotNull(config);
        assertNull(config.classes());
        assertNull(config.maxWordCount());
    }

    @Test
    void testProcessRequestEquality() {
        var r1 = ProcessRequest.builder()
                               .objectKey("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(10))
                               .build();
        var r2 = ProcessRequest.builder()
                               .objectKey("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(10))
                               .build();
        var r3 = ProcessRequest.builder()
                               .objectKey("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(20))
                               .build();

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
                                    .action(Action.TEXT_EMBEDDINGS)
                                    .build();

        assertEquals(3, request.objectKeys().size());
    }

    @Test
    void testProcessRequestActionByString() {
        var request = ProcessRequest.builder()
                                    .objectKey("path")
                                    .action("textSummarization")
                                    .action("customAction", cfg -> cfg.maxWordCount(100))
                                    .build();

        assertEquals(2, request.actions().size());
        assertNotNull(request.actions().get("textSummarization"));
        assertEquals(100, request.actions().get("customAction").maxWordCount());
    }

    @Test
    void testProcessRequestActionWithPrebuiltConfig() {
        var config = ActionConfig.builder().classes(List.of("a", "b")).build();
        var request = ProcessRequest.builder().objectKey("path").action(Action.TEXT_CLASSIFICATION, config).build();

        assertEquals(List.of("a", "b"), request.actions().get("textClassification").classes());
    }

    @Test
    void testProcessRequestActionsMapCopy() {
        var actionsMap = Map.of("textSummarization", ActionConfig.builder().maxWordCount(100).build(), "textEmbeddings",
                ActionConfig.empty());
        var request = ProcessRequest.builder().objectKey("path").actions(actionsMap).build();

        assertEquals(2, request.actions().size());
    }

    @Test
    void testProcessRequestDefaultVersion() {
        var request = ProcessRequest.builder().objectKey("path").action(Action.TEXT_EMBEDDINGS).build();
        assertEquals("context.api/v2", request.version());
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
        assertTrue(new JobStatus("j", "COMPLETED").isDone());
        assertFalse(new JobStatus("j", "Processing").isDone());
        assertFalse(new JobStatus("j", "FAILED").isDone());
        // COMPLETED with error is NOT isDone
        assertFalse(new JobStatus("j", "COMPLETED", "delivery failed").isDone());
    }

    @Test
    void testJobStatusIsCompleted() {
        assertTrue(new JobStatus("j", "Done").isCompleted());
        assertTrue(new JobStatus("j", "COMPLETED").isCompleted());
        assertTrue(new JobStatus("j", "completed").isCompleted());
        assertFalse(new JobStatus("j", "Processing").isCompleted());
        assertFalse(new JobStatus("j", "FAILED").isCompleted());
    }

    @Test
    void testJobStatusIsFailed() {
        assertTrue(new JobStatus("j", "FAILED").isFailed());
        assertTrue(new JobStatus("j", "failed").isFailed());
        assertFalse(new JobStatus("j", "Done").isFailed());
        assertFalse(new JobStatus("j", "Processing").isFailed());
    }

    @Test
    void testJobStatusIsTerminal() {
        assertTrue(new JobStatus("j", "Done").isTerminal());
        assertTrue(new JobStatus("j", "COMPLETED").isTerminal());
        assertTrue(new JobStatus("j", "FAILED").isTerminal());
        assertFalse(new JobStatus("j", "Processing").isTerminal());
        assertFalse(new JobStatus("j", "PENDING").isTerminal());
    }

    @Test
    void testJobStatusHasError() {
        assertFalse(new JobStatus("j", "Done").hasError());
        assertFalse(new JobStatus("j", "Done", null).hasError());
        assertFalse(new JobStatus("j", "Done", "").hasError());
        assertFalse(new JobStatus("j", "Done", "  ").hasError());
        assertTrue(new JobStatus("j", "COMPLETED", "delivery failed").hasError());
        assertTrue(new JobStatus("j", "FAILED", "unrecoverable pipeline error").hasError());
    }

    @Test
    void testJobStatusCompletedWithErrorIsNotDoneButIsTerminal() {
        var status = new JobStatus("j", "COMPLETED", "non-retryable delivery failure");
        assertFalse(status.isDone());
        assertTrue(status.isCompleted());
        assertTrue(status.isTerminal());
        assertTrue(status.hasError());
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
