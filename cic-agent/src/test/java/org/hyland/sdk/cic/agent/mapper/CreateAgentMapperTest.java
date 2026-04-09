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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.agent.object.AccessRight;
import org.hyland.sdk.cic.agent.object.CreateAgent;
import org.hyland.sdk.cic.agent.object.Guardrail;
import org.hyland.sdk.cic.agent.object.PrincipalType;
import org.hyland.sdk.cic.agent.object.RagParameters;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class CreateAgentMapperTest {

    @Test
    void testSerializeCreateAgent() throws JSONException {
        var agent = CreateAgent.builder("Test Agent", "Test agent description.", "test-model-v1")
                               .sourceIds(List.of("31a01094-e01a-4cc5-830b-ccca11e07a49",
                                       "6ba7b810-9dad-11d1-80b4-00c04fd430c8"))
                               .accessRights(List.of(
                                       new AccessRight(PrincipalType.GROUP, "11111111-1111-1111-1111-111111111111"),
                                       new AccessRight(PrincipalType.USER, "22222222-2222-2222-2222-222222222222")))
                               .guardrails(List.of(new Guardrail("TEST-Contextual-Grounding"),
                                       new Guardrail("TEST-Insults-High")))
                               .ragParameters(RagParameters.builder()
                                                           .limit(50)
                                                           .adjacentChunkRange(2)
                                                           .adjacentChunkMerge(true)
                                                           .rerankerEnabled(true)
                                                           .rerankerTopN(13)
                                                           .build())
                               .agentType("rag")
                               .build();

        var json = MapperService.writeAsString(agent);
        var expected = """
                {
                  "name": "Test Agent",
                  "description": "Test agent description.",
                  "modelName": "test-model-v1",
                  "avatarUrl": null,
                  "instructions": null,
                  "sourceIds": ["31a01094-e01a-4cc5-830b-ccca11e07a49", "6ba7b810-9dad-11d1-80b4-00c04fd430c8"],
                  "accessRights": [
                    {"type": "Group", "id": "11111111-1111-1111-1111-111111111111"},
                    {"type": "User", "id": "22222222-2222-2222-2222-222222222222"}
                  ],
                  "staticFilterExpression": null,
                  "dynamicFilterTemplate": null,
                  "guardrails": [
                    {"name": "TEST-Contextual-Grounding"},
                    {"name": "TEST-Insults-High"}
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

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void testSerializeMinimalCreateAgent() throws JSONException {
        var agent = CreateAgent.builder("Test Agent", "Test agent description.", "test-model-v1").build();

        var json = MapperService.writeAsString(agent);
        var expected = """
                {
                  "name": "Test Agent",
                  "description": "Test agent description.",
                  "modelName": "test-model-v1",
                  "avatarUrl": null,
                  "instructions": null,
                  "sourceIds": null,
                  "accessRights": null,
                  "staticFilterExpression": null,
                  "dynamicFilterTemplate": null,
                  "guardrails": null,
                  "ragParameters": null,
                  "agentType": null,
                  "knowledgeGraphDomainId": null
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void testMapperFactory() {
        var factory = new AgentMapperFactory();
        CICMapper<CreateAgent> mapper = factory.getMapper(CreateAgent.class);
        assertNotNull(mapper);
        assertInstanceOf(CreateAgentMapper.class, mapper);
    }
}
