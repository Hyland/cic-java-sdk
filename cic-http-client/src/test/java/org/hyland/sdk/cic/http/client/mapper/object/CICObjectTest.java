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
package org.hyland.sdk.cic.http.client.mapper.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public class CICObjectTest {

    @Test
    public void noElement() {
        var cicObject = CICObject.create();
        assertTrue(cicObject.isEmpty());
        assertFalse(cicObject.getBoolean("boolean", false));
        assertTrue(cicObject.getBoolean("boolean", true));
        assertThrows(CICSdkException.class, () -> cicObject.getBooleanOrThrow("boolean"));
        assertEquals(0, cicObject.getInt("int", 0));
        assertEquals(10, cicObject.getInt("int", 10));
        assertThrows(CICSdkException.class, () -> cicObject.getIntOrThrow("int"));
        assertEquals(0L, cicObject.getLong("long", 0L));
        assertEquals(10L, cicObject.getLong("long", 10L));
        assertThrows(CICSdkException.class, () -> cicObject.getLongOrThrow("long"));
        assertThrows(CICSdkException.class, () -> cicObject.getObjectOrThrow("object"));
        assertEquals("default", cicObject.getString("string", "default"));
        assertEquals("another", cicObject.getString("string", "another"));
        assertThrows(CICSdkException.class, () -> cicObject.getStringOrThrow("string"));
        assertNull(cicObject.getLocalDate("localDate", null));
        assertEquals(LocalDate.of(2025, 1, 1), cicObject.getLocalDate("localDate", LocalDate.of(2025, 1, 1)));
        assertThrows(CICSdkException.class, () -> cicObject.getLocalDateOrThrow("localDate"));
    }

    @Test
    public void scalarBoolean() {
        var cicObject = CICObject.create();
        cicObject.putBoolean("boolean", true);
        assertTrue(cicObject.getBoolean("boolean", false));
        assertTrue(cicObject.getBooleanOrThrow("boolean"));
        assertThrows(CICSdkException.class, () -> cicObject.getInt("boolean", 0));
        assertThrows(CICSdkException.class, () -> cicObject.getIntOrThrow("boolean"));
        assertThrows(CICSdkException.class, () -> cicObject.getLong("boolean", 0L));
        assertThrows(CICSdkException.class, () -> cicObject.getLongOrThrow("boolean"));
        assertThrows(CICSdkException.class, () -> cicObject.getString("boolean", "default"));
        assertThrows(CICSdkException.class, () -> cicObject.getStringOrThrow("boolean"));
    }

    @Test
    public void scalarInt() {
        var cicObject = CICObject.create();
        cicObject.putInt("int", 10);
        assertEquals(10, cicObject.getInt("int", 0));
        assertEquals(10, cicObject.getIntOrThrow("int"));
        assertThrows(CICSdkException.class, () -> cicObject.getBoolean("int", true));
        assertThrows(CICSdkException.class, () -> cicObject.getBooleanOrThrow("int"));
        assertEquals(10L, cicObject.getLong("int", 0));
        assertEquals(10L, cicObject.getLongOrThrow("int"));
        assertThrows(CICSdkException.class, () -> cicObject.getString("int", "default"));
        assertThrows(CICSdkException.class, () -> cicObject.getStringOrThrow("int"));
    }

    @Test
    public void scalarLong() {
        var cicObject = CICObject.create();
        cicObject.putLong("long", 10L);
        assertEquals(10L, cicObject.getLong("long", 0L));
        assertEquals(10L, cicObject.getLongOrThrow("long"));
        assertThrows(CICSdkException.class, () -> cicObject.getBoolean("long", true));
        assertThrows(CICSdkException.class, () -> cicObject.getBooleanOrThrow("long"));
        assertEquals(10, cicObject.getInt("long", 0));
        assertEquals(10, cicObject.getIntOrThrow("long"));
        assertThrows(CICSdkException.class, () -> cicObject.getString("long", "default"));
        assertThrows(CICSdkException.class, () -> cicObject.getStringOrThrow("long"));

        cicObject.putLong("long", Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, cicObject.getLong("long", 0L));
        assertEquals(Long.MAX_VALUE, cicObject.getLongOrThrow("long"));
        assertThrows(CICSdkException.class, () -> cicObject.getInt("long", 0));
        assertThrows(CICSdkException.class, () -> cicObject.getIntOrThrow("long"));
    }

    @Test
    public void scalarString() {
        var cicObject = CICObject.create();
        cicObject.putString("string", "test");
        assertEquals("test", cicObject.getString("string", "default"));
        assertEquals("test", cicObject.getStringOrThrow("string"));
        assertThrows(CICSdkException.class, () -> cicObject.getBoolean("string", true));
        assertThrows(CICSdkException.class, () -> cicObject.getBooleanOrThrow("string"));
        assertThrows(CICSdkException.class, () -> cicObject.getInt("string", 0));
        assertThrows(CICSdkException.class, () -> cicObject.getIntOrThrow("string"));
        assertThrows(CICSdkException.class, () -> cicObject.getLong("string", 0L));
        assertThrows(CICSdkException.class, () -> cicObject.getLongOrThrow("string"));
    }

    @Test
    public void scalarLocalDate() {
        var cicObject = CICObject.create();
        cicObject.putLocalDate("localDate", LocalDate.of(2025, 6, 1));
        assertEquals(LocalDate.of(2025, 6, 1), cicObject.getLocalDate("localDate", null));
        assertEquals(LocalDate.of(2025, 6, 1), cicObject.getLocalDateOrThrow("localDate"));
        assertEquals("2025-06-01", cicObject.getString("localDate", null));
        assertThrows(CICSdkException.class, () -> cicObject.getBoolean("localDate", true));
        assertThrows(CICSdkException.class, () -> cicObject.getBooleanOrThrow("localDate"));
        assertThrows(CICSdkException.class, () -> cicObject.getInt("localDate", 0));
        assertThrows(CICSdkException.class, () -> cicObject.getIntOrThrow("localDate"));
        assertThrows(CICSdkException.class, () -> cicObject.getLong("localDate", 0L));
        assertThrows(CICSdkException.class, () -> cicObject.getLongOrThrow("localDate"));
    }

    @Test
    public void nullableScalars() {
        var cicObject = CICObject.create();
        assertNull(cicObject.getStringOrNull("string"));
        assertNull(cicObject.getIntegerOrNull("int"));
        assertNull(cicObject.getDoubleOrNull("double"));

        cicObject.putNull("string");
        cicObject.putNull("int");
        cicObject.putNull("double");
        assertNull(cicObject.getStringOrNull("string"));
        assertNull(cicObject.getIntegerOrNull("int"));
        assertNull(cicObject.getDoubleOrNull("double"));

        cicObject.putString("string", "test");
        cicObject.putInt("int", 42);
        cicObject.putDouble("double", 3.14);
        assertEquals("test", cicObject.getStringOrNull("string"));
        assertEquals(42, cicObject.getIntegerOrNull("int"));
        assertEquals(3.14, cicObject.getDoubleOrNull("double"));
    }

    @Test
    public void object() {
        var nestedObject = CICObject.create();
        nestedObject.putString("string", "test");
        var cicObject = CICObject.create();
        cicObject.putObject("nested", nestedObject);
        assertEquals(nestedObject, cicObject.getObjectOrThrow("nested"));
        assertSame(nestedObject, cicObject.getObjectOrThrow("nested"));
    }

    @Test
    public void optionalString() {
        var cicObject = CICObject.create();
        assertEquals(Optional.empty(), cicObject.getOptionalString("string"));

        cicObject.putNull("string");
        assertEquals(Optional.empty(), cicObject.getOptionalString("string"));

        cicObject.putString("string", "test");
        assertEquals(Optional.of("test"), cicObject.getOptionalString("string"));
    }

    @Test
    public void optionalObject() {
        var cicObject = CICObject.create();
        assertEquals(Optional.empty(), cicObject.getOptionalObject("nested"));

        cicObject.putNull("nested");
        assertEquals(Optional.empty(), cicObject.getOptionalObject("nested"));

        var nestedObject = CICObject.create();
        cicObject.putObject("nested", nestedObject);
        assertEquals(Optional.of(nestedObject), cicObject.getOptionalObject("nested"));

        cicObject.putString("string", "test");
        assertThrows(CICSdkException.class, () -> cicObject.getOptionalObject("string"));
    }

    @Test
    public void optionalArray() {
        var cicObject = CICObject.create();
        assertEquals(Optional.empty(), cicObject.getOptionalArray("array"));

        cicObject.putNull("array");
        assertEquals(Optional.empty(), cicObject.getOptionalArray("array"));

        var array = CICArray.create();
        array.addString("element");
        cicObject.putArray("array", array);
        assertEquals(Optional.of(array), cicObject.getOptionalArray("array"));

        cicObject.putString("string", "test");
        assertThrows(CICSdkException.class, () -> cicObject.getOptionalArray("string"));
    }

    @Test
    public void toMapFlatPrimitives() {
        var cicObject = CICObject.create();
        cicObject.putString("name", "alice");
        cicObject.putInt("age", 30);
        cicObject.putLong("score", 100L);
        cicObject.putDouble("ratio", 0.5);
        cicObject.putBoolean("active", true);
        cicObject.putNull("missing");

        var map = cicObject.toMap();
        assertEquals("alice", map.get("name"));
        assertEquals(30, map.get("age"));
        assertEquals(100L, map.get("score"));
        assertEquals(0.5, map.get("ratio"));
        assertEquals(true, map.get("active"));
        assertNull(map.get("missing"));
    }

    @Test
    public void toMapNestedObject() {
        var nested = CICObject.create();
        nested.putString("key", "value");
        var cicObject = CICObject.create();
        cicObject.putObject("nested", nested);

        var map = cicObject.toMap();
        assertEquals(Map.of("key", "value"), map.get("nested"));
    }

    @Test
    public void toMapNestedArray() {
        var array = CICArray.create();
        array.addString("a");
        array.addInt(1);
        var cicObject = CICObject.create();
        cicObject.putArray("list", array);

        var map = cicObject.toMap();
        assertEquals(List.of("a", 1), map.get("list"));
    }

    @Test
    public void toMapDeeplyNested() {
        var inner = CICObject.create();
        inner.putString("leaf", "value");
        var innerArray = CICArray.create();
        innerArray.addObject(inner);
        var outer = CICObject.create();
        outer.putArray("items", innerArray);

        var map = outer.toMap();
        assertEquals(List.of(Map.of("leaf", "value")), map.get("items"));
    }
}
