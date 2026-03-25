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

import org.hyland.sdk.cic.agent.object.GuardrailsResponse;
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
                      "displayName": "Sexual content",
                      "description": "Filters content that contains sexual material.",
                      "guardrails": [
                        {"name": "HAIP-Sexual-Low", "severity": "low", "isRecommended": true},
                        {"name": "HAIP-Sexual-Medium", "severity": "medium", "isRecommended": true}
                      ]
                    },
                    {
                      "displayName": "Contextual Grounding",
                      "description": "Ensures responses are grounded in provided context.",
                      "guardrails": [
                        {"name": "HAIP-Contextual-Grounding", "severity": null, "isRecommended": true}
                      ]
                    }
                  ]
                }
                """;

        var response = MapperService.read(json, GuardrailsResponse.class);

        assertNotNull(response);
        assertEquals(2, response.guardrailGroups().size());

        var group1 = response.guardrailGroups().get(0);
        assertEquals("Sexual content", group1.displayName());
        assertEquals(2, group1.guardrails().size());
        assertEquals("HAIP-Sexual-Low", group1.guardrails().get(0).name());
        assertEquals("low", group1.guardrails().get(0).severity());
        assertTrue(group1.guardrails().get(0).isRecommended());

        var group2 = response.guardrailGroups().get(1);
        assertEquals("Contextual Grounding", group2.displayName());
        assertEquals(1, group2.guardrails().size());
        assertNull(group2.guardrails().get(0).severity());
    }
}
