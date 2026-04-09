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
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.hyland.sdk.cic.agent.object.FilterExpression;
import org.hyland.sdk.cic.agent.object.SubmitQuestionRequest;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class SubmitQuestionRequestMapperTest {

    @Test
    void testSerializeSubmitQuestionRequest() throws JSONException {
        var dynamicFilter = FilterExpression.of(
                Map.of("logicalOperator", "And", "field", "DocumentType", "value", "FinancialReport"));
        var request = SubmitQuestionRequest.builder("What is the status of the current financial report?")
                                           .contextObjectIds(List.of("123e4567-e89b-12d3-a456-426614174000",
                                                   "987e6543-e21c-32d1-b654-326614174001"))
                                           .dynamicFilter(dynamicFilter)
                                           .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "question": "What is the status of the current financial report?",
                  "contextObjectIds": [
                    "123e4567-e89b-12d3-a456-426614174000",
                    "987e6543-e21c-32d1-b654-326614174001"
                  ],
                  "dynamicFilter": {"logicalOperator": "And", "field": "DocumentType", "value": "FinancialReport"}
                }
                """;

        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeSubmitQuestionRequestMinimal() throws JSONException {
        var request = SubmitQuestionRequest.builder("What are the key highlights?").build();

        var json = MapperService.writeAsString(request);

        JSONAssert.assertEquals("{\"question\":\"What are the key highlights?\"}", json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeSubmitQuestionRequestWithoutDynamicFilter() throws JSONException {
        var request = SubmitQuestionRequest.builder("Summarise the report.")
                                           .contextObjectIds(List.of("123e4567-e89b-12d3-a456-426614174000"))
                                           .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "question": "Summarise the report.",
                  "contextObjectIds": ["123e4567-e89b-12d3-a456-426614174000"]
                }
                """;

        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testMapperFactory() {
        var factory = new AgentMapperFactory();
        CICMapper<SubmitQuestionRequest> mapper = factory.getMapper(SubmitQuestionRequest.class);
        assertNotNull(mapper);
        assertInstanceOf(SubmitQuestionRequestMapper.class, mapper);
    }
}
