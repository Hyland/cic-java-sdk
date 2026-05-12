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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.FilterExpression;
import org.hyland.sdk.cic.qna.object.StartConversationRequest;

/**
 * @since 1.0.0
 */
class StartConversationRequestMapperTest {

    @Test
    void testSerializeFull() throws JSONException {
        var dynamicFilter = FilterExpression.of(Map.of("logicalOperator", "And", "field", "Category"));
        var request = StartConversationRequest.builder("What is the status?")
                                              .contextObjectIds(List.of("obj-1", "obj-2"))
                                              .dynamicFilter(dynamicFilter)
                                              .build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "question": "What is the status?",
                  "contextObjectIds": ["obj-1", "obj-2"],
                  "dynamicFilter": {"logicalOperator": "And", "field": "Category"}
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeMinimal() throws JSONException {
        var request = StartConversationRequest.builder("Hello?").build();

        var json = MapperService.writeAsString(request);

        JSONAssert.assertEquals("{\"question\":\"Hello?\"}", json, JSONCompareMode.LENIENT);
    }

    @Test
    void testMapperFactory() {
        var factory = new QnaMapperFactory();
        CICMapper<StartConversationRequest> mapper = factory.getMapper(StartConversationRequest.class);
        assertNotNull(mapper);
        assertInstanceOf(StartConversationRequestMapper.class, mapper);
    }
}
