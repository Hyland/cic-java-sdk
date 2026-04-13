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

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.UpdateConversationRequest;

/**
 * @since 1.0.0
 */
class UpdateConversationRequestMapperTest {

    @Test
    void testSerializeFull() throws JSONException {
        var request = UpdateConversationRequest.builder("My Chat").description("A conversation about reports").build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "name": "My Chat",
                  "description": "A conversation about reports"
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeMinimal() throws JSONException {
        var request = UpdateConversationRequest.builder("Chat").build();

        var json = MapperService.writeAsString(request);

        JSONAssert.assertEquals("{\"name\":\"Chat\"}", json, JSONCompareMode.LENIENT);
    }
}
