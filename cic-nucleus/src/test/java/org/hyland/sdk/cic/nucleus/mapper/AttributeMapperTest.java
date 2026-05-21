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
package org.hyland.sdk.cic.nucleus.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.nucleus.object.Attribute;
import org.hyland.sdk.cic.nucleus.object.AttributeInput;

/**
 * @since 1.0.0
 */
class AttributeMapperTest {

    @Test
    void testDeserializeAttribute() {
        var json = """
                {
                  "key": "role",
                  "values": ["admin", "editor"]
                }
                """;

        var result = MapperService.read(json, Attribute.class);

        assertEquals("role", result.key());
        assertEquals(List.of("admin", "editor"), result.values());
    }

    @Test
    void testDeserializeAttributeList() {
        var json = """
                [
                  {"key": "role", "values": ["admin"]},
                  {"key": "department", "values": ["engineering", "product"]}
                ]
                """;

        var result = MapperService.read(json, Attribute.ListOf.class);

        assertEquals(2, result.size());
        assertEquals("role", result.get(0).key());
        assertEquals("department", result.get(1).key());
        assertEquals(List.of("engineering", "product"), result.get(1).values());
    }

    @Test
    void testSerializeAttribute() throws Exception {
        var attribute = new Attribute("role", List.of("admin"));

        var json = MapperService.writeAsString(attribute);

        JSONAssert.assertEquals("""
                {"key":"role","values":["admin"]}""", json, true);
    }

    @Test
    void testSerializeAttributeInput() throws Exception {
        var input = AttributeInput.of("role", List.of("admin", "editor"));

        var json = MapperService.writeAsString(input);

        JSONAssert.assertEquals("""
                {"key":"role","values":["admin","editor"]}""", json, true);
    }
}
