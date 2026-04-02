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
package org.hyland.sdk.cic.agent.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
import org.hyland.sdk.cic.agent.object.AgentSummary;
import org.hyland.sdk.cic.agent.object.FilterExpression;
import org.hyland.sdk.cic.agent.object.PrincipalType;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class AgentConfigurationMapperTest {

    @Test
    void testDeserializeAgentConfiguration() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Test Agent",
                  "description": "Test agent description.",
                  "modelName": "test-model-v1",
                  "avatarUrl": "https://localhost/avatar.png",
                  "avatarPresignedUrl": "https://localhost/signed-avatar.png",
                  "instructions": "Test instructions.",
                  "sourceIds": [
                    "a323a0fc-aea1-4382-9403-1d9a7c009f6c",
                    "eb6da9c9-c4aa-455c-bc95-d3aed6dca74c"
                  ],
                  "accessRights": [
                    {"type": "Group", "id": "e41f9035-e7a0-40a9-8c8b-0635bba52c5d"},
                    {"type": "User", "id": "a017ad32-a6bf-4ae1-944e-f6e4e9d51aee"}
                  ],
                  "version": 1,
                  "latest": true,
                  "agentPlatformAgentId": null,
                  "agentPlatformAgentVersionId": null,
                  "guardrails": [
                    {"name": "TEST-Contextual-Grounding"},
                    {"name": "TEST-Insults-Low"}
                  ],
                  "ragParameters": {
                    "limit": 50,
                    "adjacentChunkRange": 2,
                    "adjacentChunkMerge": true,
                    "rerankerEnabled": true,
                    "rerankerTopN": 13
                  },
                  "agentType": "rag",
                  "knowledgeGraphDomainId": null
                }
                """;

        var config = MapperService.read(json, AgentConfiguration.class);

        assertEquals("775bb838-00ac-4aef-a9c4-049589523be8", config.id());
        assertEquals("Test Agent", config.name());
        assertEquals("Test agent description.", config.description());
        assertEquals("test-model-v1", config.modelName());
        assertEquals("https://localhost/avatar.png", config.avatarUrl());
        assertEquals("https://localhost/signed-avatar.png", config.avatarPresignedUrl());
        assertEquals("Test instructions.", config.instructions());
        assertEquals(2, config.sourceIds().size());
        assertEquals("a323a0fc-aea1-4382-9403-1d9a7c009f6c", config.sourceIds().get(0));
        assertEquals(2, config.accessRights().size());
        assertEquals(PrincipalType.GROUP, config.accessRights().get(0).type());
        assertEquals(PrincipalType.USER, config.accessRights().get(1).type());
        assertEquals(1, config.version());
        assertTrue(config.latest());
        assertNull(config.agentPlatformAgentId());
        assertNull(config.agentPlatformAgentVersionId());
        assertEquals(2, config.guardrails().size());
        assertEquals("TEST-Contextual-Grounding", config.guardrails().get(0).name());
        assertNotNull(config.ragParameters());
        assertEquals(50, config.ragParameters().limit());
        assertEquals(2, config.ragParameters().adjacentChunkRange());
        assertTrue(config.ragParameters().adjacentChunkMerge());
        assertTrue(config.ragParameters().rerankerEnabled());
        assertEquals(13, config.ragParameters().rerankerTopN());
        assertEquals("rag", config.agentType());
        assertNull(config.knowledgeGraphDomainId());
    }

    @Test
    void testDeserializeAgentSummaryList() {
        var json = """
                [
                  {
                    "id": "13413629-6233-4dce-93bd-069e7f795999",
                    "name": "Test Agent",
                    "description": "Test agent description.",
                    "modelName": "test-model-v1",
                    "avatarUrl": null,
                    "instructions": null,
                    "sourceIds": [],
                    "accessRights": [],
                    "version": 1,
                    "latest": true,
                    "agentType": "rag",
                    "knowledgeGraphDomainId": null
                  }
                ]
                """;

        var summaries = MapperService.read(json, AgentSummary.ListOf.class);

        assertEquals(1, summaries.size());
        assertEquals("13413629-6233-4dce-93bd-069e7f795999", summaries.get(0).id());
        assertEquals("Test Agent", summaries.get(0).name());
    }

    @Test
    void testDeserializeAgentConfigurationWithFilterExpressions() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Agent",
                  "description": "Desc.",
                  "modelName": "model",
                  "version": 1,
                  "latest": true,
                  "staticFilterExpression": {
                    "field": "category",
                    "value": "finance",
                    "nested": {"enabled": true, "count": 3}
                  },
                  "dynamicFilterTemplate": {
                    "tags": ["reports", "quarterly"],
                    "threshold": 0.75
                  }
                }
                """;

        var config = MapperService.read(json, AgentConfiguration.class);

        assertNotNull(config.staticFilterExpression());
        assertEquals("category", config.staticFilterExpression().properties().get("field"));
        assertEquals("finance", config.staticFilterExpression().properties().get("value"));
        var nested = (Map<?, ?>) config.staticFilterExpression().properties().get("nested");
        assertNotNull(nested);
        assertEquals(Boolean.TRUE, nested.get("enabled"));
        assertEquals(3L, nested.get("count"));

        assertNotNull(config.dynamicFilterTemplate());
        assertEquals(0.75, config.dynamicFilterTemplate().properties().get("threshold"));
        var tags = (List<?>) config.dynamicFilterTemplate().properties().get("tags");
        assertNotNull(tags);
        assertEquals(List.of("reports", "quarterly"), tags);
    }

    @Test
    void testSerializeCreateAgentWithFilterExpression() throws Exception {
        var filter = FilterExpression.of(Map.of("field", "status", "value", "active"));
        var agent = org.hyland.sdk.cic.agent.object.CreateAgent.builder("Agent", "Desc", "test-model-v1")
                                                               .staticFilterExpression(filter)
                                                               .build();

        var json = MapperService.writeAsString(agent);

        org.skyscreamer.jsonassert.JSONAssert.assertEquals(
                "{\"staticFilterExpression\":{\"field\":\"status\",\"value\":\"active\"}}", json,
                org.skyscreamer.jsonassert.JSONCompareMode.LENIENT);
    }

    @Test
    void testDeserializeAgentConfigurationWithAllOptionalFieldsAbsent() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Test Agent",
                  "description": "Test agent description.",
                  "modelName": "test-model-v1",
                  "version": 1,
                  "latest": true
                }
                """;

        var config = MapperService.read(json, AgentConfiguration.class);

        assertEquals("775bb838-00ac-4aef-a9c4-049589523be8", config.id());
        assertNull(config.avatarUrl());
        assertNull(config.avatarPresignedUrl());
        assertNull(config.instructions());
        assertTrue(config.sourceIds().isEmpty());
        assertTrue(config.accessRights().isEmpty());
        assertNull(config.staticFilterExpression());
        assertNull(config.dynamicFilterTemplate());
        assertNull(config.agentPlatformAgentId());
        assertNull(config.agentPlatformAgentVersionId());
        assertTrue(config.guardrails().isEmpty());
        assertNull(config.ragParameters());
        assertNull(config.agentType());
        assertNull(config.knowledgeGraphDomainId());
    }

    @Test
    void testDeserializeAgentConfigurationWithNullRagParameters() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Agent",
                  "description": "Desc.",
                  "modelName": "test-model-v1",
                  "version": 1,
                  "latest": false,
                  "ragParameters": null
                }
                """;

        var config = MapperService.read(json, AgentConfiguration.class);

        assertNull(config.ragParameters());
    }

    @Test
    void testDeserializeAgentConfigurationWithEmptyGuardrailsAndSources() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Agent",
                  "description": "Desc.",
                  "modelName": "test-model-v1",
                  "version": 1,
                  "latest": false,
                  "guardrails": [],
                  "sourceIds": []
                }
                """;

        var config = MapperService.read(json, AgentConfiguration.class);

        assertNotNull(config.guardrails());
        assertTrue(config.guardrails().isEmpty());
        assertNotNull(config.sourceIds());
        assertTrue(config.sourceIds().isEmpty());
    }

    @Test
    void testPrincipalTypeFromValueUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> PrincipalType.fromValue("Unknown"));
    }

    @Test
    void testPrincipalTypeFromValueNullThrows() {
        assertThrows(NullPointerException.class, () -> PrincipalType.fromValue(null));
    }

    @Test
    void testDeserializeAccessRightWithUnknownPrincipalTypeThrows() {
        var json = """
                {
                  "id": "775bb838-00ac-4aef-a9c4-049589523be8",
                  "name": "Agent",
                  "description": "Desc.",
                  "modelName": "model",
                  "version": 1,
                  "latest": false,
                  "accessRights": [
                    {"type": "UnknownType", "id": "11111111-1111-1111-1111-111111111111"}
                  ]
                }
                """;

        assertThrows(IllegalArgumentException.class, () -> MapperService.read(json, AgentConfiguration.class));
    }
}
