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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ke.object.Action;
import org.hyland.sdk.cic.ke.object.ActionDescriptor;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.HealthDetails;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;
import org.hyland.sdk.cic.ke.object.VersionUsageStats;

/**
 * @since 1.0.0
 */
class KEMapperFactoryTest {

    @Test
    void testMapperFactoryProvidesAllMappers() {
        var factory = new KEMapperFactory();

        assertNotNull(factory.getMapper(PresignResponse.class));
        assertNotNull(factory.getMapper(JobStatus.class));
        assertNotNull(factory.getMapper(EmbeddingModel.class));
        assertNotNull(factory.getMapper(ProcessingOptions.class));
        assertNotNull(factory.getMapper(PresignedUrl.class));
        assertNotNull(factory.getMapper(ProcessRequest.class));
        assertNotNull(factory.getMapper(EnrichmentResult.class));
        assertNotNull(factory.getMapper(ConfigOptions.class));
        assertNotNull(factory.getMapper(ConfigRule.class));
        assertNotNull(factory.getMapper(RuleTestRequest.class));
        assertNotNull(factory.getMapper(RuleTestResponse.class));
        assertNotNull(factory.getMapper(ActionDescriptor.class));
        assertNotNull(factory.getMapper(ActionDescriptor.ListOf.class));
        assertNotNull(factory.getMapper(VersionUsageStats.class));
        assertNotNull(factory.getMapper(HealthDetails.class));
    }

    @Test
    void testMapperFactoryReturnsNullForUnknownType() {
        var factory = new KEMapperFactory();
        assertNull(factory.getMapper(String.class));
    }

    @Test
    void testPresignResponseDeserialization() {
        var json = """
                {
                  "job_id": "abc-123",
                  "put_url": "https://s3.example.com/put",
                  "get_url": "https://s3.example.com/get",
                  "options": {"pii": false}
                }
                """;

        var result = MapperService.read(json, PresignResponse.class);
        assertEquals("abc-123", result.jobId());
        assertEquals("https://s3.example.com/put", result.putUrl());
        assertEquals("https://s3.example.com/get", result.getUrl());
        assertNotNull(result.options());
    }

    @Test
    void testJobStatusDeserialization() {
        var json = """
                {
                  "jobId": "job-1",
                  "status": "Done"
                }
                """;

        var result = MapperService.read(json, JobStatus.class);
        assertEquals("job-1", result.jobId());
        assertEquals("Done", result.status());
        assertEquals(true, result.isDone());
    }

    @Test
    void testPresignedUrlDeserialization() {
        var json = """
                {
                  "presignedUrl": "https://upload.example.com",
                  "objectKey": "contents/file.pdf"
                }
                """;

        var result = MapperService.read(json, PresignedUrl.class);
        assertEquals("https://upload.example.com", result.presignedUrl());
        assertEquals("contents/file.pdf", result.objectKey());
    }

    @Test
    void testProcessRequestV2Serialization() {
        var request = ProcessRequest.builder()
                                    .objectPath("contents/file.pdf")
                                    .action(Action.TEXT_SUMMARIZATION, cfg -> cfg.maxWordCount(150))
                                    .action(Action.IMAGE_DESCRIPTION)
                                    .build();

        var json = MapperService.writeAsString(request);
        assertNotNull(json);
        assertTrue(json.contains("\"version\":\"context.api/v2\""));
        assertTrue(json.contains("\"textSummarization\""));
        assertTrue(json.contains("\"imageDescription\""));
    }

    @Test
    void testProcessingOptionsSerialization() {
        var options = ProcessingOptions.builder()
                                       .chunking(true)
                                       .chunkSize(2000)
                                       .chunkingStrategy("context")
                                       .embedding(true)
                                       .embeddingsModel("cohere.embed-multilingual-v3")
                                       .pii(pii -> pii.mode("redaction"))
                                       .build();

        var json = MapperService.writeAsString(options);
        assertNotNull(json);

        var deserialized = MapperService.read(json, ProcessingOptions.class);
        assertEquals(true, deserialized.chunking());
        assertEquals(2000, deserialized.chunkSize());
        assertEquals("context", deserialized.chunkingStrategy());
        assertEquals(true, deserialized.embedding());
        assertEquals("cohere.embed-multilingual-v3", deserialized.embeddingsModel());
        assertNotNull(deserialized.pii());
        assertEquals("redaction", deserialized.pii().mode());
    }

    @Test
    void testConfigOptionsDeserialization() {
        var json = """
                {
                  "defaults": {
                    "chunking": true,
                    "chunk_size": 1000,
                    "embedding": false
                  },
                  "rules": [
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
                  ]
                }
                """;

        var result = MapperService.read(json, ConfigOptions.class);
        assertNotNull(result.defaults());
        assertEquals(true, result.defaults().chunking());
        assertEquals(1000, result.defaults().chunkSize());
        assertEquals(1, result.rules().size());
        assertEquals("PDF Rule", result.rules().get(0).name());
        assertEquals(1, result.rules().get(0).conditions().size());
        assertEquals("content_type", result.rules().get(0).conditions().get(0).field());
    }

    @Test
    void testEnrichmentResultDeserialization() {
        var json = """
                {
                  "id": "proc-123",
                  "timestamp": "2026-01-01T00:00:00Z",
                  "status": "SUCCESS",
                  "inProgress": false,
                  "results": [
                    {
                      "objectKey": "contents/file.pdf",
                      "textSummary": {
                        "isSuccess": true,
                        "result": "This is a summary.",
                        "error": null
                      },
                      "imageDescription": {
                        "isSuccess": false,
                        "result": null,
                        "error": "Not an image"
                      }
                    }
                  ]
                }
                """;

        var result = MapperService.read(json, EnrichmentResult.class);
        assertEquals("proc-123", result.id());
        assertEquals("SUCCESS", result.status());
        assertEquals(false, result.inProgress());
        assertEquals(1, result.results().size());

        var entry = result.results().get(0);
        assertEquals("contents/file.pdf", entry.objectKey());
        assertNotNull(entry.textSummary());
        assertEquals(true, entry.textSummary().isSuccess());
        assertEquals("This is a summary.", entry.textSummary().result().value());
        assertNotNull(entry.imageDescription());
        assertEquals(false, entry.imageDescription().isSuccess());
        assertEquals("Not an image", entry.imageDescription().error().message());
    }

    @Test
    void testRuleTestRequestSerialization() {
        var request = RuleTestRequest.builder()
                                     .property("content_type", "application/pdf")
                                     .property("size", "1024")
                                     .build();

        var json = MapperService.writeAsString(request);
        assertNotNull(json);
    }

    @Test
    void testRuleTestResponseDeserialization() {
        var json = """
                {
                  "matchedRule": {
                    "id": "rule-1",
                    "name": "PDF Rule",
                    "conditions": [
                      {"field": "content_type", "value": "application/pdf"}
                    ],
                    "config": {
                      "chunking": true
                    }
                  },
                  "effectiveConfig": {
                    "chunking": true,
                    "embedding": false
                  }
                }
                """;

        var result = MapperService.read(json, RuleTestResponse.class);
        assertNotNull(result.matchedRule());
        assertEquals("PDF Rule", result.matchedRule().name());
        assertNotNull(result.effectiveConfig());
        assertEquals(true, result.effectiveConfig().chunking());
    }
}
