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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.AgentConfiguration;
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
                  "name": "Data Processing Agent",
                  "description": "This agent is configured for data processing tasks.",
                  "modelName": "bedrock-amazon-nova-micro",
                  "avatarUrl": "http://www.example.com/avatar.png",
                  "avatarPresignedUrl": "https://example.com/signed-avatar.png",
                  "instructions": "Follow the data guidelines strictly.",
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
                    {"name": "HAIP-Contextual-Grounding"},
                    {"name": "HAIP-Insults-Low"}
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

        assertEquals(UUID.fromString("775bb838-00ac-4aef-a9c4-049589523be8"), config.id());
        assertEquals("Data Processing Agent", config.name());
        assertEquals("This agent is configured for data processing tasks.", config.description());
        assertEquals("bedrock-amazon-nova-micro", config.modelName());
        assertEquals("http://www.example.com/avatar.png", config.avatarUrl());
        assertEquals("https://example.com/signed-avatar.png", config.avatarPresignedUrl());
        assertEquals("Follow the data guidelines strictly.", config.instructions());
        assertEquals(2, config.sourceIds().size());
        assertEquals(UUID.fromString("a323a0fc-aea1-4382-9403-1d9a7c009f6c"), config.sourceIds().get(0));
        assertEquals(2, config.accessRights().size());
        assertEquals(PrincipalType.GROUP, config.accessRights().get(0).type());
        assertEquals(PrincipalType.USER, config.accessRights().get(1).type());
        assertEquals(1, config.version());
        assertTrue(config.latest());
        assertNull(config.agentPlatformAgentId());
        assertNull(config.agentPlatformAgentVersionId());
        assertEquals(2, config.guardrails().size());
        assertEquals("HAIP-Contextual-Grounding", config.guardrails().get(0).name());
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
                    "name": "Data Processing Agent",
                    "description": "This agent is configured for data processing tasks.",
                    "modelName": "bedrock-amazon-nova-micro",
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

        var summaries = MapperService.read(json, org.hyland.sdk.cic.agent.object.AgentSummary.ListOf.class);

        assertEquals(1, summaries.size());
        assertEquals(UUID.fromString("13413629-6233-4dce-93bd-069e7f795999"), summaries.get(0).id());
        assertEquals("Data Processing Agent", summaries.get(0).name());
    }
}
