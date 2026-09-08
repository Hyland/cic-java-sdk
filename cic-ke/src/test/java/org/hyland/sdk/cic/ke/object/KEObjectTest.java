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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class KEObjectTest {

    // --- Action enum ---

    @Test
    void testActionValuesMatchSupportedContract() {
        var actionValues = Arrays.stream(Action.values())
                                 .map(Action::value)
                                 .collect(java.util.stream.Collectors.toSet());

        assertEquals(
                Set.of("imageClassification", "imageDescription", "imageEmbeddings", "imageMetadataGeneration",
                        "namedEntityRecognitionImage", "namedEntityRecognitionText", "pretrainedClassification",
                        "textClassification", "textEmbeddings", "textMetadataGeneration", "textSummarization"),
                actionValues);
    }

    @Test
    void testInternalGraphActionsAreNotExposed() {
        var actionValues = Arrays.stream(Action.values()).map(Action::value).toList();

        assertFalse(actionValues.contains("globalEntities"));
        assertFalse(actionValues.contains("localEntities"));
        assertFalse(actionValues.contains("domainGlossaryExtraction"));
        assertFalse(actionValues.contains("globalEntitiesExtraction"));
        assertFalse(actionValues.contains("localEntitiesExtraction"));
    }

    // --- ActionConfig builder ---

    @Test
    void testActionConfigEmpty() {
        var config = ActionConfig.empty();
        assertTrue(config.classes().isEmpty());
        assertNull(config.maxWordCount());
        assertTrue(config.kSimilarMetadata().isEmpty());
        assertTrue(config.instructions().isEmpty());
    }

    @Test
    void testActionConfigBuilderWithAllFields() {
        var config = ActionConfig.builder()
                                 .classes(List.of("invoice", "contract"))
                                 .maxWordCount(200)
                                 .addSimilarMetadata(Map.of("title", "Sample"))
                                 .instructions(Map.of("context", "legal documents"))
                                 .category("MediaType")
                                 .model("pretrained-model-a")
                                 .build();

        assertEquals(List.of("invoice", "contract"), config.classes());
        assertEquals(200, config.maxWordCount());
        assertEquals(1, config.kSimilarMetadata().size());
        assertEquals("legal documents", config.instructions().get("context"));
        assertEquals("MediaType", config.category());
        assertEquals("pretrained-model-a", config.model());
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
                                    .objectPath("contents/file.pdf")
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
    void testProcessRequestBuilderEmptyActionsThrows() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> ProcessRequest.builder().objectPath("contents/file.pdf").build());

        assertEquals("At least one action is required", exception.getMessage());
    }

    @Test
    void testProcessRequestNullOptionalFields() {
        var request = ProcessRequest.builder().objectPath("path").action(Action.TEXT_EMBEDDINGS).build();

        assertEquals(ProcessRequest.VERSION_V2, request.version());
        var config = request.actions().get("textEmbeddings");
        assertNotNull(config);
        assertTrue(config.classes().isEmpty());
        assertNull(config.maxWordCount());
    }

    @Test
    void testProcessRequestEquality() {
        var r1 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(10))
                               .build();
        var r2 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(10))
                               .build();
        var r3 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(20))
                               .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }

    @Test
    void testProcessRequestObjectKeysReplacePreviouslyAddedKeys() {
        var request = ProcessRequest.builder()
                                    .objectPath("path1")
                                    .objectPath("path2")
                                    .objectKeys(List.of(ObjectKey.forPath("path3")))
                                    .action(Action.TEXT_EMBEDDINGS)
                                    .build();

        assertEquals(List.of(ObjectKey.forPath("path3")), request.objectKeys());
    }

    @Test
    void testProcessRequestSupportsMultipleObjectKeys() {
        var request = ProcessRequest.builder()
                                    .objectKeys(List.of(ObjectKey.forPath("path1"), ObjectKey.forPath("path2")))
                                    .action(Action.TEXT_EMBEDDINGS)
                                    .build();

        assertEquals(2, request.objectKeys().size());
    }

    @Test
    void testProcessRequestSupportsPathAndDocumentIdKeys() {
        var request = ProcessRequest.builder()
                                    .objectPath("contents/file.pdf")
                                    .documentId("document-123")
                                    .action(Action.TEXT_EMBEDDINGS)
                                    .build();

        assertEquals(ObjectKey.forPath("contents/file.pdf"), request.objectKeys().get(0));
        assertEquals(ObjectKey.forDocumentId("document-123"), request.objectKeys().get(1));
        assertThrows(UnsupportedOperationException.class,
                () -> request.objectKeys().add(ObjectKey.forPath("another-path")));
    }

    @Test
    void testProcessRequestRejectsNullObjectKeys() {
        assertThrows(NullPointerException.class, () -> ProcessRequest.builder().objectKey(null));
        assertThrows(NullPointerException.class, () -> ProcessRequest.builder().objectKeys(null));
        assertThrows(NullPointerException.class,
                () -> ProcessRequest.builder().objectKeys(Arrays.asList(ObjectKey.forPath("path"), null)));
    }

    @Test
    void testProcessRequestActionByString() {
        var request = ProcessRequest.builder()
                                    .objectPath("path")
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
        var request = ProcessRequest.builder().objectPath("path").action(Action.TEXT_CLASSIFICATION, config).build();

        assertEquals(List.of("a", "b"), request.actions().get("textClassification").classes());
    }

    @Test
    void testProcessRequestActionsMapCopy() {
        var actionsMap = Map.of("textSummarization", ActionConfig.builder().maxWordCount(100).build(), "textEmbeddings",
                ActionConfig.empty());
        var request = ProcessRequest.builder().objectPath("path").actions(actionsMap).build();

        assertEquals(2, request.actions().size());
    }

    @Test
    void testProcessRequestDefaultVersion() {
        var request = ProcessRequest.builder().objectPath("path").action(Action.TEXT_EMBEDDINGS).build();
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
        var success = new ActionResult.Success<>("result-value");
        assertTrue(success.isSuccess());
        assertEquals("result-value", success.result());
        assertNull(success.error());

        var failure = new ActionResult.Failure<>(new ProcessingError(ProcessingErrorType.UNKNOWN, "error-msg"));
        assertFalse(failure.isSuccess());
        assertEquals("error-msg", failure.error().message());
    }

    @Test
    void testObjectKey() {
        var path = ObjectKey.forPath("contents/file.pdf");
        var document = ObjectKey.forDocumentId("document-123");

        assertEquals("contents/file.pdf", path.path());
        assertNull(path.documentId());
        assertNull(document.path());
        assertEquals("document-123", document.documentId());
        assertThrows(IllegalArgumentException.class, () -> new ObjectKey(null, null));
        assertThrows(IllegalArgumentException.class, () -> new ObjectKey("", null));
        assertThrows(IllegalArgumentException.class, () -> new ObjectKey("path", "document-123"));
    }

    @Test
    void testPresignedUrl() {
        var url = new PresignedUrl("https://upload.url", "contents/file.pdf");
        assertEquals("https://upload.url", url.presignedUrl());
        assertEquals("contents/file.pdf", url.objectKey());
    }

    @Test
    void testPresignResponse() {
        var response = new PresignResponse("job-1", "https://put.url", "https://get.url", null);
        assertEquals("job-1", response.jobId());
        assertEquals("https://put.url", response.putUrl());
        assertEquals("https://get.url", response.getUrl());
        assertNull(response.options());
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
    void testEmbeddingModel() {
        var model = new EmbeddingModel("cohere.embed-multilingual-v3", 512, List.of("float32", "int8"), List.of(1024),
                List.of("search_document"));
        assertEquals("cohere.embed-multilingual-v3", model.name());
        assertEquals(512, model.maxChunkSize());
        assertEquals(List.of("float32", "int8"), model.supportedPrecisions());
        assertEquals(List.of(1024), model.supportedOutputDimensions());
        assertEquals(List.of("search_document"), model.supportedInputType());
    }

    @Test
    void testEnrichmentResultEntry() {
        var textSummary = new ActionResult.Success<>("Summary");
        var entry = new EnrichmentResultEntry("key", null, null, null, textSummary, null, null, null, null, null, null,
                null, null);
        assertEquals("key", entry.objectKey());
        assertNotNull(entry.textSummary());
        assertEquals("Summary", entry.textSummary().result());
        assertNull(entry.imageDescription());
        assertNull(entry.pretrainedClassification());
        assertTrue(entry.generalProcessingErrors().isEmpty());
    }

    @Test
    void testEnrichmentResultEntryDefensivelyCopiesProcessingErrors() {
        var errors = new ArrayList<>(
                List.of(new ProcessingError(ProcessingErrorType.VALIDATION_ERROR, "Unsupported format")));
        var entry = new EnrichmentResultEntry("key", null, null, null, null, null, null, null, null, null, null, null,
                errors);

        errors.clear();

        assertEquals(List.of(new ProcessingError(ProcessingErrorType.VALIDATION_ERROR, "Unsupported format")),
                entry.generalProcessingErrors());
        assertThrows(UnsupportedOperationException.class,
                () -> entry.generalProcessingErrors().add(new ProcessingError(ProcessingErrorType.UNKNOWN, "Failure")));
    }

    @Test
    void testEnrichmentResultEntryWithPretrainedClassification() {
        var classification = new ClassificationResult("invoice", 0.95);
        var actionResult = new ActionResult.Success<>(classification);
        var entry = new EnrichmentResultEntry("key", null, null, null, null, null, null, null, null, null, null,
                actionResult, null);
        assertNotNull(entry.pretrainedClassification());
        assertTrue(entry.pretrainedClassification().isSuccess());
        assertEquals("invoice", entry.pretrainedClassification().result().classification());
        assertEquals(0.95, entry.pretrainedClassification().result().confidence());
    }

    // --- ClassificationResult ---

    @Test
    void testClassificationResult() {
        var result = new ClassificationResult("contract", 0.87);
        assertEquals("contract", result.classification());
        assertEquals(0.87, result.confidence());
    }

    // --- ActionDescriptor ---

    @Test
    void testActionDescriptor() {
        var descriptor = new ActionDescriptor("pretrainedClassification", List.of("model-a", "model-b"),
                List.of("category-x", "category-y"));
        assertEquals("pretrainedClassification", descriptor.name());
        assertEquals(List.of("model-a", "model-b"), descriptor.availableModels());
        assertEquals(List.of("category-x", "category-y"), descriptor.availableCategories());
    }

    @Test
    void testActionDescriptorNullableFields() {
        var descriptor = new ActionDescriptor("textSummarization", null, null);
        assertEquals("textSummarization", descriptor.name());
        assertTrue(descriptor.availableModels().isEmpty());
        assertTrue(descriptor.availableCategories().isEmpty());
    }

    @Test
    void testActionDescriptorListOf() {
        var list = new ActionDescriptor.ListOf();
        list.add(new ActionDescriptor("a", null, null));
        list.add(new ActionDescriptor("b", List.of("m1"), List.of("c1")));
        assertEquals(2, list.size());
    }

    // --- ProcessingErrorType enum ---

    @Test
    void testProcessingErrorTypeFromValueKnownTypes() {
        assertEquals(ProcessingErrorType.LAMBDA_ERROR, ProcessingErrorType.fromValue("LambdaError"));
        assertEquals(ProcessingErrorType.TIMEOUT, ProcessingErrorType.fromValue("Timeout"));
        assertEquals(ProcessingErrorType.VALIDATION_ERROR, ProcessingErrorType.fromValue("ValidationError"));
        assertEquals(ProcessingErrorType.LAMBDA_RESPONSE, ProcessingErrorType.fromValue("LambdaResponse"));
        assertEquals(ProcessingErrorType.UNEXPECTED_ERROR, ProcessingErrorType.fromValue("UnexpectedError"));
        assertEquals(ProcessingErrorType.AUTHORIZATION_ERROR, ProcessingErrorType.fromValue("AuthorizationError"));
        assertEquals(ProcessingErrorType.DESERIALIZATION_ERROR, ProcessingErrorType.fromValue("DeserializationError"));
        assertEquals(ProcessingErrorType.GUARDRAIL_VIOLATION, ProcessingErrorType.fromValue("GuardrailViolation"));
    }

    @Test
    void testProcessingErrorTypeFromValueUnknown() {
        assertEquals(ProcessingErrorType.UNKNOWN, ProcessingErrorType.fromValue("SomeFutureError"));
        assertEquals(ProcessingErrorType.UNKNOWN, ProcessingErrorType.fromValue(null));
    }

    @Test
    void testProcessingErrorTypeValues() {
        assertEquals("LambdaError", ProcessingErrorType.LAMBDA_ERROR.value());
        assertNull(ProcessingErrorType.UNKNOWN.value());
    }

    @Test
    void testProcessingErrorType() {
        var error = new ProcessingError(ProcessingErrorType.VALIDATION_ERROR, "Unsupported format");
        assertEquals(ProcessingErrorType.VALIDATION_ERROR, error.type());
        assertEquals("Unsupported format", error.message());

        var unknown = new ProcessingError(ProcessingErrorType.UNKNOWN, "Something new");
        assertEquals(ProcessingErrorType.UNKNOWN, unknown.type());

        var nullMessage = new ProcessingError(ProcessingErrorType.TIMEOUT, null);
        assertEquals(ProcessingErrorType.TIMEOUT, nullMessage.type());
        assertNull(nullMessage.message());
    }

    // --- ProcessRequest saveResultInContentLakeRepository ---

    @Test
    void testProcessRequestSaveResultInContentLakeRepositoryDefault() {
        var request = ProcessRequest.builder().objectPath("path").action(Action.TEXT_EMBEDDINGS).build();
        assertFalse(request.saveResultInContentLakeRepository());
    }

    @Test
    void testProcessRequestSaveResultInContentLakeRepositoryTrue() {
        var request = ProcessRequest.builder()
                                    .objectPath("path")
                                    .action(Action.TEXT_EMBEDDINGS)
                                    .saveResultInContentLakeRepository(true)
                                    .build();
        assertTrue(request.saveResultInContentLakeRepository());
    }

    @Test
    void testProcessRequestEqualityWithSaveResult() {
        var r1 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION)
                               .saveResultInContentLakeRepository(true)
                               .build();
        var r2 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION)
                               .saveResultInContentLakeRepository(true)
                               .build();
        var r3 = ProcessRequest.builder()
                               .objectPath("a")
                               .action(Action.TEXT_SUMMARIZATION)
                               .saveResultInContentLakeRepository(false)
                               .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
