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
package org.hyland.sdk.cic.ke.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;
import org.hyland.sdk.cic.ke.object.ProcessResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;

/**
 * @since 1.0.0
 */
class KEMapperTest {

    // --- ProcessRequestMapper ---

    @Test
    void testSerializeProcessRequestWithAllFields() throws JSONException {
        var request = ProcessRequest.builder()
                                    .objectKey("contents/file.pdf")
                                    .action("text-summarization")
                                    .action("image-description")
                                    .addClass("invoice")
                                    .addClass("receipt")
                                    .addSimilarMetadata(Map.of("title", "Sample Doc"))
                                    .maxWordCount(150)
                                    .instructions("Summarize in bullet points")
                                    .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "objectKeys": [{"path": "contents/file.pdf"}],
                  "actions": ["text-summarization", "image-description"],
                  "classes": ["invoice", "receipt"],
                  "kSimilarMetadata": [{"title": "Sample Doc"}],
                  "maxWordCount": 150,
                  "instructions": "Summarize in bullet points"
                }
                """;
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void testSerializeProcessRequestOmitsNullOptionalFields() throws JSONException {
        var request = ProcessRequest.builder().objectKey("contents/img.jpg").action("image-description").build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "objectKeys": [{"path": "contents/img.jpg"}],
                  "actions": ["image-description"]
                }
                """;
        JSONAssert.assertEquals(expected, json, false);
        assertFalse(json.contains("instructions"));
        assertFalse(json.contains("maxWordCount"));
        assertFalse(json.contains("classes"));
        assertFalse(json.contains("kSimilarMetadata"));
    }

    // --- ProcessingOptionsMapper ---

    @Test
    void testProcessingOptionsRoundTripWithAllFields() throws JSONException {
        var json = """
                {
                  "normalization": {"quotations": true, "dashes": false},
                  "chunking": true,
                  "chunking_strategy": "context",
                  "chunk_size": 2000,
                  "embedding": true,
                  "embeddings_model": "cohere.embed-multilingual-v3",
                  "json_schema": "my-schema",
                  "pii": {"mode": "redaction", "entity_redaction": true}
                }
                """;

        var options = MapperService.read(json, ProcessingOptions.class);

        assertNotNull(options.normalization());
        assertEquals(true, options.normalization().quotations());
        assertEquals(false, options.normalization().dashes());
        assertEquals(true, options.chunking());
        assertEquals("context", options.chunkingStrategy());
        assertEquals(2000, options.chunkSize());
        assertEquals(true, options.embedding());
        assertEquals("cohere.embed-multilingual-v3", options.embeddingsModel());
        assertEquals("my-schema", options.jsonSchema());
        assertNotNull(options.pii());
        assertEquals("redaction", options.pii().mode());
        assertEquals(true, options.pii().entityRedaction());

        var serialized = MapperService.writeAsString(options);
        JSONAssert.assertEquals(json, serialized, true);
    }

    @Test
    void testProcessingOptionsPartialFields() {
        var json = """
                {"chunking": false, "embedding": true}
                """;

        var options = MapperService.read(json, ProcessingOptions.class);

        assertEquals(false, options.chunking());
        assertEquals(true, options.embedding());
        assertNull(options.normalization());
        assertNull(options.pii());
        assertNull(options.chunkingStrategy());
        assertNull(options.chunkSize());
        assertNull(options.embeddingsModel());
    }

    // --- ConfigRuleMapper ---

    @Test
    void testConfigRuleRoundTrip() throws JSONException {
        var json = """
                {
                  "id": "rule-1",
                  "name": "PDF Rule",
                  "conditions": [
                    {"field": "content_type", "value": "application/pdf"}
                  ],
                  "config": {
                    "chunking": true,
                    "chunk_size": 2000
                  }
                }
                """;

        var rule = MapperService.read(json, ConfigRule.class);

        assertEquals("rule-1", rule.id());
        assertEquals("PDF Rule", rule.name());
        assertEquals(1, rule.conditions().size());
        assertEquals("content_type", rule.conditions().get(0).field());
        assertEquals("application/pdf", rule.conditions().get(0).value());
        assertNotNull(rule.config());
        assertEquals(true, rule.config().chunking());
        assertEquals(2000, rule.config().chunkSize());

        var serialized = MapperService.writeAsString(rule);
        assertNotNull(serialized);
        assertTrue(serialized.contains("PDF Rule"));
        assertTrue(serialized.contains("content_type"));
    }

    @Test
    void testConfigRuleListDeserialization() {
        var json = """
                [
                  {"id": "r1", "name": "Rule One", "conditions": [], "config": {"chunking": true}},
                  {"id": "r2", "name": "Rule Two", "conditions": [], "config": {"embedding": true}}
                ]
                """;

        var rules = MapperService.read(json, ConfigRule.ListOf.class);

        assertEquals(2, rules.size());
        assertEquals("Rule One", rules.get(0).name());
        assertEquals("Rule Two", rules.get(1).name());
    }

    // --- EmbeddingModelMapper ---

    @Test
    void testEmbeddingModelDeserialization() {
        var json = """
                {"id": "cohere-v3", "name": "Cohere Embed Multilingual v3"}
                """;

        var model = MapperService.read(json, EmbeddingModel.class);

        assertEquals("cohere-v3", model.id());
        assertEquals("Cohere Embed Multilingual v3", model.name());
    }

    @Test
    void testEmbeddingModelListDeserialization() {
        var json = """
                [
                  {"id": "model-1", "name": "Model One"},
                  {"id": "model-2", "name": "Model Two"}
                ]
                """;

        var models = MapperService.read(json, EmbeddingModel.ListOf.class);

        assertEquals(2, models.size());
        assertEquals("model-1", models.get(0).id());
        assertEquals("model-2", models.get(1).id());
    }

    // --- EnrichmentResultMapper ---

    @Test
    void testEnrichmentResultWithAllActionTypes() {
        var json = """
                {
                  "id": "proc-1",
                  "timestamp": "2026-01-01T00:00:00Z",
                  "status": "SUCCESS",
                  "inProgress": false,
                  "results": [{
                    "objectKey": "contents/doc.pdf",
                    "textSummary": {"isSuccess": true, "result": "Summary text", "error": null},
                    "imageDescription": {"isSuccess": true, "result": "A cat on a mat", "error": null},
                    "textClassification": {"isSuccess": true, "result": "invoice", "error": null},
                    "imageClassification": {"isSuccess": true, "result": "photo", "error": null},
                    "textEmbeddings": {"isSuccess": true, "result": [0.1, 0.2, 0.3], "error": null},
                    "imageEmbeddings": {"isSuccess": true, "result": [0.4, 0.5], "error": null},
                    "namedEntityText": {
                      "isSuccess": true,
                      "result": {"PERSON": ["Alice", "Bob"], "ORG": ["Hyland"]},
                      "error": null
                    },
                    "namedEntityImage": {"isSuccess": false, "result": null, "error": "Not supported"},
                    "imageMetadata": {
                      "isSuccess": true,
                      "result": {"camera": "Canon", "iso": "200"},
                      "error": null
                    },
                    "textMetadata": {
                      "isSuccess": true,
                      "result": {"author": "John", "subject": "Report"},
                      "error": null
                    }
                  }]
                }
                """;

        var result = MapperService.read(json, EnrichmentResult.class);

        assertEquals("proc-1", result.id());
        assertTrue(result.isSuccess());
        assertTrue(result.isComplete());
        assertEquals(1, result.results().size());

        var entry = result.results().get(0);
        assertEquals("contents/doc.pdf", entry.objectKey());

        assertTrue(entry.textSummary().isSuccess());
        assertEquals("Summary text", entry.textSummary().result());

        assertTrue(entry.imageDescription().isSuccess());
        assertEquals("A cat on a mat", entry.imageDescription().result());

        assertTrue(entry.textClassification().isSuccess());
        assertEquals("invoice", entry.textClassification().result());

        assertEquals(List.of(0.1, 0.2, 0.3), entry.textEmbeddings().result());
        assertEquals(List.of(0.4, 0.5), entry.imageEmbeddings().result());

        assertTrue(entry.namedEntityText().isSuccess());
        assertEquals(List.of("Alice", "Bob"), entry.namedEntityText().result().get("PERSON"));
        assertEquals(List.of("Hyland"), entry.namedEntityText().result().get("ORG"));

        assertFalse(entry.namedEntityImage().isSuccess());
        assertEquals("Not supported", entry.namedEntityImage().error());

        assertEquals("Canon", entry.imageMetadata().result().get("camera"));
        assertEquals("John", entry.textMetadata().result().get("author"));
    }

    @Test
    void testEnrichmentResultWithIntegerEmbeddingValues() {
        var json = """
                {
                  "id": "proc-int",
                  "timestamp": "2026-01-01T00:00:00Z",
                  "status": "SUCCESS",
                  "inProgress": false,
                  "results": [{
                    "objectKey": "contents/doc.pdf",
                    "textEmbeddings": {"isSuccess": true, "result": [0, 1, 0.5, 2, -1], "error": null},
                    "imageEmbeddings": {"isSuccess": true, "result": [1, 0, -3, 0.75], "error": null}
                  }]
                }
                """;

        var result = MapperService.read(json, EnrichmentResult.class);
        var entry = result.results().get(0);

        assertEquals(List.of(0.0, 1.0, 0.5, 2.0, -1.0), entry.textEmbeddings().result());
        assertEquals(List.of(1.0, 0.0, -3.0, 0.75), entry.imageEmbeddings().result());
    }

    @Test
    void testEnrichmentResultWithNullActionResults() {
        var json = """
                {
                  "id": "proc-2",
                  "timestamp": "2026-01-01T00:00:00Z",
                  "status": "SUCCESS",
                  "inProgress": false,
                  "results": [{
                    "objectKey": "contents/img.jpg"
                  }]
                }
                """;

        var result = MapperService.read(json, EnrichmentResult.class);
        var entry = result.results().get(0);

        assertNull(entry.textSummary());
        assertNull(entry.imageDescription());
        assertNull(entry.textEmbeddings());
        assertNull(entry.imageEmbeddings());
        assertNull(entry.namedEntityText());
        assertNull(entry.namedEntityImage());
        assertNull(entry.imageMetadata());
        assertNull(entry.textMetadata());
        assertNull(entry.textClassification());
        assertNull(entry.imageClassification());
    }

    @Test
    void testEnrichmentResultInProgress() {
        var json = """
                {
                  "id": "proc-3",
                  "timestamp": "2026-01-01T00:00:00Z",
                  "status": "PROCESSING",
                  "inProgress": true,
                  "results": []
                }
                """;

        var result = MapperService.read(json, EnrichmentResult.class);

        assertFalse(result.isSuccess());
        assertFalse(result.isComplete());
        assertTrue(result.results().isEmpty());
    }

    // --- ProcessResponseMapper ---

    @Test
    void testProcessResponseDeserialization() {
        var json = """
                {"processingId": "proc-abc-123"}
                """;

        var response = MapperService.read(json, ProcessResponse.class);

        assertEquals("proc-abc-123", response.processingId());
    }

    // --- PresignResponseMapper ---

    @Test
    void testPresignResponseDeserialization() throws JSONException {
        var json = """
                {
                  "job_id": "job-abc",
                  "put_url": "https://s3.example.com/put",
                  "get_url": "https://s3.example.com/get"
                }
                """;

        var response = MapperService.read(json, PresignResponse.class);

        assertEquals("job-abc", response.jobId());
        assertEquals("https://s3.example.com/put", response.putUrl());
        assertEquals("https://s3.example.com/get", response.getUrl());
    }

    // --- PresignedUrlMapper ---

    @Test
    void testPresignedUrlDeserialization() {
        var json = """
                {
                  "presignedUrl": "https://upload.example.com/signed",
                  "objectKey": "contents/document.pdf"
                }
                """;

        var presignedUrl = MapperService.read(json, PresignedUrl.class);

        assertEquals("https://upload.example.com/signed", presignedUrl.presignedUrl());
        assertEquals("contents/document.pdf", presignedUrl.objectKey());
    }

    // --- JobStatusMapper ---

    @Test
    void testJobStatusDeserialization() {
        var json = """
                {"jobId": "j-1", "status": "Done"}
                """;

        var status = MapperService.read(json, JobStatus.class);

        assertEquals("j-1", status.jobId());
        assertEquals("Done", status.status());
        assertTrue(status.isDone());
    }

    @Test
    void testJobStatusProcessing() {
        var json = """
                {"jobId": "j-2", "status": "Processing"}
                """;

        var status = MapperService.read(json, JobStatus.class);

        assertFalse(status.isDone());
    }

    // --- ConfigOptionsMapper ---

    @Test
    void testConfigOptionsDeserialization() {
        var json = """
                {
                  "defaults": {
                    "chunking": true,
                    "chunk_size": 1500,
                    "embedding": false
                  },
                  "rules": [
                    {
                      "id": "r1",
                      "name": "PDF Rule",
                      "conditions": [{"field": "content_type", "value": "application/pdf"}],
                      "config": {"chunking": true, "chunk_size": 3000}
                    },
                    {
                      "id": "r2",
                      "name": "Image Rule",
                      "conditions": [{"field": "content_type", "value": "image/jpeg"}],
                      "config": {"embedding": true}
                    }
                  ]
                }
                """;

        var config = MapperService.read(json, ConfigOptions.class);

        assertNotNull(config.defaults());
        assertEquals(true, config.defaults().chunking());
        assertEquals(1500, config.defaults().chunkSize());
        assertEquals(false, config.defaults().embedding());

        assertEquals(2, config.rules().size());
        assertEquals("PDF Rule", config.rules().get(0).name());
        assertEquals("Image Rule", config.rules().get(1).name());
        assertEquals(3000, config.rules().get(0).config().chunkSize());
    }

    // --- RuleTestRequestMapper ---

    @Test
    void testRuleTestRequestSerialization() throws JSONException {
        var request = RuleTestRequest.builder()
                                     .property("content_type", "application/pdf")
                                     .property("size", "1024")
                                     .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "content_type": "application/pdf",
                  "size": "1024"
                }
                """;
        JSONAssert.assertEquals(expected, json, true);
    }

    // --- RuleTestResponseMapper ---

    @Test
    void testRuleTestResponseDeserialization() {
        var json = """
                {
                  "matchedRule": {
                    "id": "r1",
                    "name": "PDF Rule",
                    "conditions": [{"field": "content_type", "value": "application/pdf"}],
                    "config": {"chunking": true}
                  },
                  "effectiveConfig": {
                    "chunking": true,
                    "embedding": false,
                    "chunk_size": 2000
                  }
                }
                """;

        var response = MapperService.read(json, RuleTestResponse.class);

        assertNotNull(response.matchedRule());
        assertEquals("PDF Rule", response.matchedRule().name());
        assertEquals(1, response.matchedRule().conditions().size());

        assertNotNull(response.effectiveConfig());
        assertEquals(true, response.effectiveConfig().chunking());
        assertEquals(false, response.effectiveConfig().embedding());
        assertEquals(2000, response.effectiveConfig().chunkSize());
    }

    @Test
    void testRuleTestResponseNoMatch() {
        var json = """
                {
                  "matchedRule": null,
                  "effectiveConfig": {"chunking": false}
                }
                """;

        var response = MapperService.read(json, RuleTestResponse.class);

        assertNull(response.matchedRule());
        assertNotNull(response.effectiveConfig());
        assertEquals(false, response.effectiveConfig().chunking());
    }
}
