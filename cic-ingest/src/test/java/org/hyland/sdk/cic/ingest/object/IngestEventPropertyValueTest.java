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
package org.hyland.sdk.cic.ingest.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ingest.object.IngestEventPropertyValue.Type;

/**
 * @since 1.1.0
 */
class IngestEventPropertyValueTest {

    @Test
    public void testBuilderNull() {
        var property = IngestEventPropertyValue.builderNull().build();

        assertEquals(Type.OBJECT, property.type());
        assertInstanceOf(CICPrimitive.class, property.value());
        assertEquals(null, property.value().toJavaValue());
    }

    @Test
    public void testBuilderBoolean() {
        var property = IngestEventPropertyValue.builder(true).build();
        assertEquals(Type.BOOLEAN, property.type());
        assertEquals(true, property.value().toJavaValue());
    }

    @Test
    public void testBuilderBooleanArray() {
        var property = IngestEventPropertyValue.builder(true, false, true).build();
        assertEquals(Type.BOOLEAN, property.type());
        assertInstanceOf(CICArray.class, property.value());
        assertEquals(List.of(true, false, true), property.value().toJavaValue());
    }

    @Test
    public void testBuilderBooleanRawArray() {
        var property = IngestEventPropertyValue.builder(new boolean[] { true, false }).build();
        assertEquals(Type.BOOLEAN, property.type());
        assertEquals(List.of(true, false), property.value().toJavaValue());
    }

    @Test
    public void testBuilderInt() {
        var property = IngestEventPropertyValue.builder(42).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(42, property.value().toJavaValue());
    }

    @Test
    public void testBuilderIntArray() {
        var property = IngestEventPropertyValue.builder(1, 2, 3).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(List.of(1, 2, 3), property.value().toJavaValue());
    }

    @Test
    public void testBuilderIntRawArray() {
        var property = IngestEventPropertyValue.builder(new int[] { 1, 2, 3 }).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(List.of(1, 2, 3), property.value().toJavaValue());
    }

    @Test
    public void testBuilderLong() {
        var property = IngestEventPropertyValue.builder(42L).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(42L, property.value().toJavaValue());
    }

    @Test
    public void testBuilderLongArray() {
        var property = IngestEventPropertyValue.builder(1L, 2L, 3L).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(List.of(1L, 2L, 3L), property.value().toJavaValue());
    }

    @Test
    public void testBuilderLongRawArray() {
        var property = IngestEventPropertyValue.builder(new long[] { 1L, 2L }).build();
        assertEquals(Type.INTEGER, property.type());
        assertEquals(List.of(1L, 2L), property.value().toJavaValue());
    }

    @Test
    public void testBuilderDouble() {
        var property = IngestEventPropertyValue.builder(1.5d).build();
        assertEquals(Type.FLOAT, property.type());
        assertEquals(1.5d, property.value().toJavaValue());
    }

    @Test
    public void testBuilderDoubleArray() {
        var property = IngestEventPropertyValue.builder(1.5d, 2.5d).build();
        assertEquals(Type.FLOAT, property.type());
        assertEquals(List.of(1.5d, 2.5d), property.value().toJavaValue());
    }

    @Test
    public void testBuilderDoubleRawArray() {
        var property = IngestEventPropertyValue.builder(new double[] { 1.5d, 2.5d }).build();
        assertEquals(Type.FLOAT, property.type());
        assertEquals(List.of(1.5d, 2.5d), property.value().toJavaValue());
    }

    @Test
    public void testBuilderString() {
        var property = IngestEventPropertyValue.builder("hello").build();
        assertEquals(Type.STRING, property.type());
        assertEquals("hello", property.value().toJavaValue());
    }

    @Test
    public void testBuilderStringArray() {
        var property = IngestEventPropertyValue.builder("a", "b", "c").build();
        assertEquals(Type.STRING, property.type());
        assertInstanceOf(CICArray.class, property.value());
        assertEquals(List.of("a", "b", "c"), property.value().toJavaValue());
    }

    @Test
    public void testBuilderInstant() {
        var instant = Instant.parse("2026-01-01T00:00:00Z");
        var property = IngestEventPropertyValue.builder(instant).build();
        assertEquals(Type.DATETIME, property.type());
        assertEquals(instant.toString(), property.value().toJavaValue());
    }

    @Test
    public void testBuilderInstantArray() {
        var instant1 = Instant.parse("2026-01-01T00:00:00Z");
        var instant2 = Instant.parse("2026-02-01T00:00:00Z");
        var property = IngestEventPropertyValue.builder(instant1, instant2).build();
        assertEquals(Type.DATETIME, property.type());
        assertEquals(List.of(instant1.toString(), instant2.toString()), property.value().toJavaValue());
    }

    @Test
    public void testBuilderMap() {
        var property = IngestEventPropertyValue.builder(Map.of("key", "value")).build();
        assertEquals(Type.OBJECT, property.type());
        assertInstanceOf(CICObject.class, property.value());
    }

    @Test
    public void testBuilderMapArray() {
        var property = IngestEventPropertyValue.builder(Map.of("key", "value1"), Map.of("key", "value2")).build();
        assertEquals(Type.OBJECT, property.type());
        assertInstanceOf(CICArray.class, property.value());
    }

    @Test
    public void testBuilderNullValueThrows() {
        assertThrows(NullPointerException.class, () -> IngestEventPropertyValue.builder((String) null));
        assertThrows(NullPointerException.class, () -> IngestEventPropertyValue.builder((Instant) null));
        assertThrows(NullPointerException.class, () -> IngestEventPropertyValue.builder((Map<String, ?>) null));
        assertThrows(NullPointerException.class, () -> IngestEventPropertyValue.builder("a", (String[]) null));
    }

    @Test
    public void testAnnotationAndExtras() {
        var property = IngestEventPropertyValue.builder("value")
                                               .annotation("name")
                                               .extra("custom", 42L)
                                               .extras(Map.of("other", "thing"))
                                               .build();

        assertEquals("name", property.extras().get("annotation").toJavaValue());
        assertEquals(42L, property.extras().get("custom").toJavaValue());
        assertEquals("thing", property.extras().get("other").toJavaValue());
    }

    @Test
    public void testExtraRejectsNullKey() {
        var builder = IngestEventPropertyValue.builder("value");
        assertThrows(NullPointerException.class, () -> builder.extra(null, "v"));
    }

    @Test
    public void testExtraRejectsReservedKeys() {
        var builder = IngestEventPropertyValue.builder("value");
        assertThrows(IllegalArgumentException.class, () -> builder.extra("type", "v"));
        assertThrows(IllegalArgumentException.class, () -> builder.extra("value", "v"));
    }

    @Test
    public void testExtrasRejectsNullMap() {
        var builder = IngestEventPropertyValue.builder("value");
        assertThrows(NullPointerException.class, () -> builder.extras(null));
    }

    @Test
    public void testEqualsAndHashCode() {
        var property1 = IngestEventPropertyValue.builder("hello").annotation("name").build();
        var property2 = IngestEventPropertyValue.builder("hello").annotation("name").build();
        var property3 = IngestEventPropertyValue.builder("world").annotation("name").build();

        assertEquals(property1, property2);
        assertEquals(property1.hashCode(), property2.hashCode());
        assertNotEquals(property1, property3);
        assertFalse(property1.equals("not a property"));
    }

    @Test
    public void testToString() {
        var noExtras = IngestEventPropertyValue.builder("hello").build();
        assertTrue(noExtras.toString().contains("type=STRING"));
        assertTrue(noExtras.toString().contains("value="));

        var withExtras = IngestEventPropertyValue.builder("hello").annotation("name").build();
        assertTrue(withExtras.toString().contains("annotation"));
    }

    @Test
    public void testTypeFrom() {
        assertEquals(Type.BOOLEAN, Type.from("boolean"));
        assertEquals(Type.DATETIME, Type.from("datetime"));
        assertEquals(Type.FLOAT, Type.from("float"));
        assertEquals(Type.INTEGER, Type.from("integer"));
        assertEquals(Type.OBJECT, Type.from("object"));
        assertEquals(Type.STRING, Type.from("string"));
    }

    @Test
    public void testTypeFromUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> Type.from("unknown"));
    }

    @Test
    public void testTypeLabel() {
        assertEquals("string", Type.STRING.label());
    }
}
