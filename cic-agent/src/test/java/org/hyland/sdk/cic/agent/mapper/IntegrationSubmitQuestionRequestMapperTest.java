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

import org.hyland.sdk.cic.agent.object.IntegrationSubmitQuestionRequest;
import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
class IntegrationSubmitQuestionRequestMapperTest {

    @Test
    void testSerializeIntegrationSubmitQuestionRequest() throws JSONException {
        var userId = "12345678-90ab-cdef-1234-567890abcdef";
        var request = IntegrationSubmitQuestionRequest.builder("What is the status of my latest order?", userId)
                                                      .contextObjectIds(List.of("123e4567-e89b-12d3-a456-426614174000",
                                                              "987e6543-e21c-32d1-b654-326614174001",
                                                              "456e7890-e12d-43f2-c789-426614174002"))
                                                      .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "question": "What is the status of my latest order?",
                  "userId": "12345678-90ab-cdef-1234-567890abcdef",
                  "contextObjectIds": [
                    "123e4567-e89b-12d3-a456-426614174000",
                    "987e6543-e21c-32d1-b654-326614174001",
                    "456e7890-e12d-43f2-c789-426614174002"
                  ]
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void testSerializeIntegrationSubmitQuestionRequestMinimal() throws JSONException {
        var userId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
        var request = IntegrationSubmitQuestionRequest.builder("Summarise recent activity.", userId).build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "question": "Summarise recent activity.",
                  "userId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
                }
                """;

        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void testMapperFactory() {
        var factory = new AgentMapperFactory();
        CICMapper<IntegrationSubmitQuestionRequest> mapper = factory.getMapper(IntegrationSubmitQuestionRequest.class);
        assertNotNull(mapper);
        assertInstanceOf(IntegrationSubmitQuestionRequestMapper.class, mapper);
    }
}
