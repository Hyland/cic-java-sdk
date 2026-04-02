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

import java.time.LocalDate;

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
                    "displayName": "Mock Model Alpha",
                    "modelName": "mock-model-alpha-v1",
                    "status": "Active",
                    "eolDate": null,
                    "replacementModelName": null
                  },
                  {
                    "displayName": "Mock Model Beta",
                    "modelName": "mock-model-beta-v1",
                    "status": "Deprecated",
                    "eolDate": null,
                    "replacementModelName": null
                  }
                ]
                """;

        var models = MapperService.read(json, LlmModel.ListOf.class);

        assertEquals(2, models.size());
        assertEquals("Mock Model Alpha", models.get(0).displayName());
        assertEquals("mock-model-alpha-v1", models.get(0).modelName());
        assertEquals("Active", models.get(0).status());
        assertNull(models.get(0).eolDate());
        assertEquals("Deprecated", models.get(1).status());
    }

    @Test
    void testDeserializeLlmModelListMultiple() {
        var json = """
                [
                  {
                    "displayName": "Mock Model Alpha",
                    "modelName": "mock-model-alpha-v1",
                    "status": "Active",
                    "eolDate": null,
                    "replacementModelName": null
                  },
                  {
                    "displayName": "Mock Model Beta",
                    "modelName": "mock-model-beta-v1",
                    "status": "Active",
                    "eolDate": null,
                    "replacementModelName": null
                  },
                  {
                    "displayName": "Mock Model Gamma",
                    "modelName": "mock-model-gamma-v1",
                    "status": "Deprecated",
                    "eolDate": null,
                    "replacementModelName": null
                  }
                ]
                """;

        var models = MapperService.read(json, LlmModel.ListOf.class);

        assertEquals(3, models.size());
        assertEquals("Mock Model Alpha", models.get(0).displayName());
        assertEquals("mock-model-alpha-v1", models.get(0).modelName());
        assertEquals("Active", models.get(0).status());
        assertEquals("Mock Model Beta", models.get(1).displayName());
        assertEquals("mock-model-beta-v1", models.get(1).modelName());
        assertEquals("Deprecated", models.get(2).status());
    }

    @Test
    void testDeserializeLlmModelWithReplacementModelName() {
        var json = """
                [
                  {
                    "displayName": "Old Model",
                    "modelName": "old-model-v1:0",
                    "status": "Deprecated",
                    "eolDate": "2025-06-01",
                    "replacementModelName": "new-model-v2:0"
                  }
                ]
                """;

        var models = MapperService.read(json, LlmModel.ListOf.class);

        assertEquals(1, models.size());
        assertEquals("old-model-v1:0", models.get(0).modelName());
        assertEquals("Deprecated", models.get(0).status());
        assertEquals(LocalDate.of(2025, 6, 1), models.get(0).eolDate());
        assertEquals("new-model-v2:0", models.get(0).replacementModelName());
    }
}
