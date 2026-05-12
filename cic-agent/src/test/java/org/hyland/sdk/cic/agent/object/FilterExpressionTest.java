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
package org.hyland.sdk.cic.agent.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0.0
 */
class FilterExpressionTest {

    @Test
    void testNullValueAllowed() {
        var expr = FilterExpression.of(Map.of("key", "value"));
        var map = new HashMap<>(expr.properties());
        map.put("nullable", null);
        var withNull = FilterExpression.of(map);

        assertNull(withNull.properties().get("nullable"));
    }

    @Test
    void testNullKeyThrows() {
        var map = new HashMap<String, Object>();
        map.put(null, "value");

        assertThrows(NullPointerException.class, () -> FilterExpression.of(map));
    }

    @Test
    void testNullPropertiesThrows() {
        assertThrows(NullPointerException.class, () -> FilterExpression.of(null));
    }

    @Test
    void testPropertiesIsUnmodifiable() {
        var expr = FilterExpression.of(Map.of("k", "v"));

        assertThrows(UnsupportedOperationException.class, () -> expr.properties().put("k2", "v2"));
    }

    @Test
    void testMutatingSourceMapDoesNotAffectExpression() {
        var source = new HashMap<String, Object>();
        source.put("k", "original");
        var expr = FilterExpression.of(source);

        source.put("k", "mutated");

        assertEquals("original", expr.properties().get("k"));
    }

    @Test
    void testAllowedValueTypes() {
        var map = new HashMap<String, Object>();
        map.put("str", "hello");
        map.put("int", 1);
        map.put("long", 1L);
        map.put("double", 1.5);
        map.put("bool", true);
        map.put("null", null);
        var expr = FilterExpression.of(map);

        assertEquals(6, expr.properties().size());
        assertTrue(expr.properties().containsKey("null"));
        assertNull(expr.properties().get("null"));
    }
}
