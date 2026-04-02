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

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.GuardrailGroup.ListOf;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class GuardrailsResponseMapperTest {

    @Test
    void testDeserializeGuardrailsResponse() {
        var json = """
                {
                  "guardrailGroups": [
                    {
                      "displayName": "Mock content",
                      "description": "Filters mock content.",
                      "guardrails": [
                        {"name": "TEST-Mock-Low", "severity": "low", "isRecommended": true},
                        {"name": "TEST-Mock-Medium", "severity": "medium", "isRecommended": true}
                      ]
                    },
                    {
                      "displayName": "Contextual Grounding",
                      "description": "Ensures responses are grounded in provided context.",
                      "guardrails": [
                        {"name": "TEST-Contextual-Grounding", "severity": null, "isRecommended": true}
                      ]
                    }
                  ]
                }
                """;

        var groups = MapperService.read(json, ListOf.class);

        assertNotNull(groups);
        assertEquals(2, groups.size());

        var group1 = groups.get(0);
        assertEquals("Mock content", group1.displayName());
        assertEquals(2, group1.guardrails().size());
        assertEquals("TEST-Mock-Low", group1.guardrails().get(0).name());
        assertEquals("low", group1.guardrails().get(0).severity());
        assertTrue(group1.guardrails().get(0).isRecommended());

        var group2 = groups.get(1);
        assertEquals("Contextual Grounding", group2.displayName());
        assertEquals(1, group2.guardrails().size());
        assertNull(group2.guardrails().get(0).severity());
    }

    @Test
    void testDeserializeGuardrailsResponseMultipleGroups() {
        var json = """
                {
                  "guardrailGroups": [
                    {
                      "displayName": "Mock content",
                      "description": "Filters mock content including explicit material.",
                      "guardrails": [
                        {"name": "TEST-Mock-Low", "severity": "low", "isRecommended": true},
                        {"name": "TEST-Mock-Medium", "severity": "medium", "isRecommended": true},
                        {"name": "TEST-Mock-High", "severity": "high", "isRecommended": true}
                      ]
                    },
                    {
                      "displayName": "Mock violence",
                      "description": "Filters mock violent material.",
                      "guardrails": [
                        {"name": "TEST-Violence-Low", "severity": "low", "isRecommended": true},
                        {"name": "TEST-Violence-Medium", "severity": "medium", "isRecommended": true},
                        {"name": "TEST-Violence-High", "severity": "high", "isRecommended": true}
                      ]
                    },
                    {
                      "displayName": "Contextual Grounding",
                      "description": "Ensures responses are grounded in provided context and source material.",
                      "guardrails": [
                        {"name": "TEST-Contextual-Grounding", "severity": null, "isRecommended": true}
                      ]
                    }
                  ]
                }
                """;

        var groups = MapperService.read(json, ListOf.class);

        assertEquals(3, groups.size());

        var group1 = groups.get(0);
        assertEquals("Mock content", group1.displayName());
        assertEquals(3, group1.guardrails().size());
        assertEquals("TEST-Mock-Low", group1.guardrails().get(0).name());
        assertEquals("low", group1.guardrails().get(0).severity());
        assertTrue(group1.guardrails().get(0).isRecommended());
        assertEquals("high", group1.guardrails().get(2).severity());

        var group2 = groups.get(1);
        assertEquals("Mock violence", group2.displayName());
        assertEquals(3, group2.guardrails().size());
        assertEquals("TEST-Violence-Medium", group2.guardrails().get(1).name());

        var grounding = groups.get(2);
        assertEquals("Contextual Grounding", grounding.displayName());
        assertEquals(1, grounding.guardrails().size());
        assertEquals("TEST-Contextual-Grounding", grounding.guardrails().get(0).name());
        assertNull(grounding.guardrails().get(0).severity());
        assertTrue(grounding.guardrails().get(0).isRecommended());
    }
}
