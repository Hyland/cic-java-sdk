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
package org.hyland.sdk.cic.qna.mapper;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.FilterExpression;
import org.hyland.sdk.cic.qna.object.Filters;
import org.hyland.sdk.cic.qna.object.IntegrationType;
import org.hyland.sdk.cic.qna.object.SubmitQuestionRequest;

/**
 * @since 1.0.0
 */
class SubmitQuestionRequestMapperTest {

    @Test
    void testSerializeFull() throws JSONException {
        var merged = FilterExpression.of(Map.of("logicalOperator", "And"));
        var filters = Filters.builder().merged(merged).hxqlFilter(null).build();
        var request = SubmitQuestionRequest.builder()
                                           .questionId("5320120c-0aa8-44e8-b3c5-af0152528307")
                                           .question("What is the status?")
                                           .contextObjectIds(List.of("obj123", "obj456"))
                                           .userId("bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168")
                                           .integrationType(IntegrationType.HX)
                                           .agentId("f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a")
                                           .agentVersion(2)
                                           .modelName("gpt4")
                                           .instructions("Provide a concise summary.")
                                           .sourceIds(List.of("1faa8521-44a2-44dd-bb9b-d2fd013386bc"))
                                           .filters(filters)
                                           .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "questionId": "5320120c-0aa8-44e8-b3c5-af0152528307",
                  "question": "What is the status?",
                  "contextObjectIds": ["obj123", "obj456"],
                  "userId": "bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168",
                  "externalUserId": null,
                  "integrationType": "Hx",
                  "agentId": "f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a",
                  "agentVersion": 2,
                  "modelName": "gpt4",
                  "instructions": "Provide a concise summary.",
                  "sourceIds": ["1faa8521-44a2-44dd-bb9b-d2fd013386bc"],
                  "filters": {
                    "merged": {"logicalOperator": "And"},
                    "hxqlFilter": null
                  }
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeMinimal() throws JSONException {
        var request = SubmitQuestionRequest.builder()
                                           .questionId("5320120c-0aa8-44e8-b3c5-af0152528307")
                                           .question("What?")
                                           .userId("bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168")
                                           .integrationType(IntegrationType.ALFRESCO)
                                           .agentId("f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a")
                                           .agentVersion(1)
                                           .modelName("gpt4")
                                           .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "questionId": "5320120c-0aa8-44e8-b3c5-af0152528307",
                  "question": "What?",
                  "userId": "bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168",
                  "integrationType": "Alfresco",
                  "agentId": "f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a",
                  "agentVersion": 1,
                  "modelName": "gpt4"
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    public void testSerializationIntegrationTypeWithDifferentCase() throws JSONException {
        var request = SubmitQuestionRequest.builder()
                                           .questionId("5320120c-0aa8-44e8-b3c5-af0152528307")
                                           .question("What is the status?")
                                           .userId("bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168")
                                           .integrationType(IntegrationType.fromValue("hx"))
                                           .agentId("f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a")
                                           .agentVersion(2)
                                           .modelName("gpt4")
                                           .build();
        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "questionId": "5320120c-0aa8-44e8-b3c5-af0152528307",
                  "question": "What is the status?",
                  "userId": "bbf6cd75-7d6f-4125-b8cf-e9e0f3ce3168",
                  "integrationType": "Hx",
                  "agentId": "f33b9d9e-6f8c-49e9-adfc-e954aefc5d2a",
                  "agentVersion": 2,
                  "modelName": "gpt4"
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }
}
