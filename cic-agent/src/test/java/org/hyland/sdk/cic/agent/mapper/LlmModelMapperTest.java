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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.agent.object.LlmModel;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class LlmModelMapperTest {

    @Test
    void testDeserializeLlmModelList() {
        var json = """
                [
                  {
                    "displayName": "Amazon Nova Micro",
                    "modelName": "amazon.nova-micro-v1:0",
                    "status": "Active",
                    "eolDate": null,
                    "replacementModelName": null
                  },
                  {
                    "displayName": "Meta Llama 3.2 11B",
                    "modelName": "meta.llama3-2-11b-instruct-v1:0",
                    "status": "Deprecated",
                    "eolDate": null,
                    "replacementModelName": null
                  }
                ]
                """;

        var models = MapperService.read(json, LlmModel.List.class);

        assertEquals(2, models.size());
        assertEquals("Amazon Nova Micro", models.get(0).displayName());
        assertEquals("amazon.nova-micro-v1:0", models.get(0).modelName());
        assertEquals("Active", models.get(0).status());
        assertNull(models.get(0).eolDate());
        assertEquals("Deprecated", models.get(1).status());
    }
}
