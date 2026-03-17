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
package org.hyland.sdk.cic.http.client.mapper.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * Tests for double and null value handling in Jackson2 serialization.
 *
 * @since 1.0.0
 */
class Jackson2SerializerDoubleAndNullTest {

    private final Jackson2Serializer serializer = new Jackson2SerializerFactory().getSerializer();

    @Test
    void testSerializeDoubleInObject() {
        var cicObject = CICObject.create();
        cicObject.putString("name", "Test");
        cicObject.putDouble("price", 19.99);
        cicObject.putDouble("rating", 4.5);

        String json = serializer.writeAsString(cicObject);

        assertEquals("{\"name\":\"Test\",\"price\":19.99,\"rating\":4.5}", json);
    }

    @Test
    void testDeserializeDoubleInObject() {
        String json = "{\"name\":\"Test\",\"price\":19.99,\"rating\":4.5}";

        CICObject cicObject = (CICObject) serializer.read(json);

        assertEquals("Test", cicObject.getString("name", null));
        assertEquals(19.99, cicObject.getDouble("price", 0.0), 0.001);
        assertEquals(4.5, cicObject.getDouble("rating", 0.0), 0.001);
    }

    @Test
    void testSerializeDoubleInArray() {
        var cicArray = CICArray.create();
        cicArray.addString("item");
        cicArray.addDouble(3.14);
        cicArray.addDouble(2.71);

        String json = serializer.writeAsString(cicArray);

        assertEquals("[\"item\",3.14,2.71]", json);
    }

    @Test
    void testDeserializeDoubleInArray() {
        String json = "[\"item\",3.14,2.71]";

        CICArray cicArray = (CICArray) serializer.read(json);

        assertEquals("item", cicArray.getString(0));
        assertEquals(3.14, cicArray.getDouble(1), 0.001);
        assertEquals(2.71, cicArray.getDouble(2), 0.001);
    }

    @Test
    void testDeserializeNullInObjectIsSkipped() {
        String json = "{\"name\":\"Test\",\"nullField\":null,\"active\":true}";

        CICObject cicObject = (CICObject) serializer.read(json);

        assertEquals("Test", cicObject.getString("name", null));
        assertEquals(true, cicObject.getBoolean("active", false));
        // Null field should not be present in the object
        assertFalse(cicObject.getProperties().containsKey("nullField"));
    }

    @Test
    void testDeserializeNullInArrayIsSkipped() {
        String json = "[\"item1\",null,\"item2\",null]";

        CICArray cicArray = (CICArray) serializer.read(json);

        // Only non-null values should be in the array
        assertEquals(2, cicArray.getElements().size());
        assertEquals("item1", cicArray.getString(0));
        assertEquals("item2", cicArray.getString(1));
    }

    @Test
    void testSerializeNullExplicitly() {
        var cicObject = CICObject.create();
        cicObject.putString("name", "Test");
        cicObject.getProperties()
                 .put("explicitNull", new org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICNull());

        String json = serializer.writeAsString(cicObject);

        assertEquals("{\"name\":\"Test\",\"explicitNull\":null}", json);
    }

    @Test
    void testRoundTripWithDoubleValues() {
        var original = CICObject.create();
        original.putDouble("pi", 3.14159);
        original.putInt("count", 42);

        var nestedArray = CICArray.create();
        nestedArray.addDouble(1.1);
        nestedArray.addDouble(2.2);
        original.putArray("values", nestedArray);

        String json = serializer.writeAsString(original);
        CICObject deserialized = (CICObject) serializer.read(json);

        assertEquals(3.14159, deserialized.getDouble("pi", 0.0), 0.00001);
        assertEquals(42, deserialized.getInt("count", 0));
        CICArray deserializedArray = deserialized.getArrayOrThrow("values");
        assertEquals(1.1, deserializedArray.getDouble(0), 0.001);
        assertEquals(2.2, deserializedArray.getDouble(1), 0.001);
    }
}
