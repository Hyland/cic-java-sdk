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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.http.client.mapper.jackson2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public class Jackson2SerializerTest {

    protected final Jackson2Serializer serializer = new Jackson2SerializerFactory().getSerializer();

    @Test
    public void readEmptyObject() {
        var cicObject = (CICObject) serializer.read("{}");
        assertNotNull(cicObject);
        assertTrue(cicObject.isEmpty());
    }

    @Test
    public void readScalarBoolean() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "boolean": true
                }""");
        assertNotNull(cicObject);
        assertTrue(cicObject.getBooleanOrThrow("boolean"));
    }

    @Test
    public void readScalarInt() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "int": 10
                }""");
        assertNotNull(cicObject);
        assertEquals(10, cicObject.getIntOrThrow("int"));
    }

    @Test
    public void readScalarLong() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "long": %s
                }""".formatted(Long.MAX_VALUE));
        assertNotNull(cicObject);
        assertEquals(Long.MAX_VALUE, cicObject.getLongOrThrow("long"));
    }

    @Test
    public void readScalarString() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "string": "test"
                }""");
        assertNotNull(cicObject);
        assertEquals("test", cicObject.getStringOrThrow("string"));
    }

    @Test
    public void readObject() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "nested": {
                    "string": "test"
                  }
                }""");
        assertNotNull(cicObject);
        var nestedObject = cicObject.getObjectOrThrow("nested");
        assertEquals("test", nestedObject.getStringOrThrow("string"));
    }

    @Test
    public void readArray() {
        var cicObject = (CICObject) serializer.read("""
                {
                  "array": [
                    "test"
                  ]
                }""");
        assertNotNull(cicObject);
        var array = cicObject.getArrayOrThrow("array");
        assertEquals("test", array.getString(0));
    }
}
