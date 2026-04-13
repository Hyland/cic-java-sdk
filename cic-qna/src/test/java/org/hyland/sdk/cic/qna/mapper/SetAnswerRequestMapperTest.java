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

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.SetAnswerReference;
import org.hyland.sdk.cic.qna.object.SetAnswerRequest;

/**
 * @since 1.0.0
 */
class SetAnswerRequestMapperTest {

    @Test
    void testSerializeFull() throws JSONException {
        var refs = List.of(new SetAnswerReference("ref123", "doc123", 1.0),
                new SetAnswerReference("ref456", "doc456", 2.0));
        var request = SetAnswerRequest.builder("This is the answer.").references(refs).build();

        var json = MapperService.writeAsString(request);
        var expected = """
                {
                  "answer": "This is the answer.",
                  "references": [
                    {"referenceId": "ref123", "objectId": "doc123", "rankScore": 1.0},
                    {"referenceId": "ref456", "objectId": "doc456", "rankScore": 2.0}
                  ]
                }
                """;
        JSONAssert.assertEquals(expected, json, JSONCompareMode.LENIENT);
    }

    @Test
    void testSerializeMinimal() throws JSONException {
        var request = SetAnswerRequest.builder("Just an answer.").build();

        var json = MapperService.writeAsString(request);

        JSONAssert.assertEquals("{\"answer\":\"Just an answer.\"}", json, JSONCompareMode.LENIENT);
    }
}
