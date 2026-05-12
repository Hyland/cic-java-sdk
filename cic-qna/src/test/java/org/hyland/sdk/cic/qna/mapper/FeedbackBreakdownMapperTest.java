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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.qna.object.FeedbackBreakdown;

/**
 * @since 1.0.0
 */
class FeedbackBreakdownMapperTest {

    @Test
    void testDeserialize() {
        var json = """
                {
                  "good": 10,
                  "bad": 3,
                  "regenerate": 2,
                  "none": 85,
                  "totalAnswers": 100
                }
                """;

        var breakdown = MapperService.read(json, FeedbackBreakdown.class);

        assertEquals(10, breakdown.good());
        assertEquals(3, breakdown.bad());
        assertEquals(2, breakdown.regenerate());
        assertEquals(85, breakdown.none());
        assertEquals(100, breakdown.totalAnswers());
    }

    @Test
    void testDeserializeZeros() {
        var json = """
                {
                  "good": 0,
                  "bad": 0,
                  "regenerate": 0,
                  "none": 0,
                  "totalAnswers": 0
                }
                """;

        var breakdown = MapperService.read(json, FeedbackBreakdown.class);

        assertEquals(0, breakdown.good());
        assertEquals(0, breakdown.bad());
        assertEquals(0, breakdown.regenerate());
        assertEquals(0, breakdown.none());
        assertEquals(0, breakdown.totalAnswers());
    }
}
