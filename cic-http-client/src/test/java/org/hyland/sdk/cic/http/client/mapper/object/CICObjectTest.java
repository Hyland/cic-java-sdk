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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void object() {
        var nestedObject = CICObject.create();
        nestedObject.putString("string", "test");
        var cicObject = CICObject.create();
        cicObject.putObject("nested", nestedObject);
        assertEquals(nestedObject, cicObject.getObjectOrThrow("nested"));
        assertSame(nestedObject, cicObject.getObjectOrThrow("nested"));
    }
}
