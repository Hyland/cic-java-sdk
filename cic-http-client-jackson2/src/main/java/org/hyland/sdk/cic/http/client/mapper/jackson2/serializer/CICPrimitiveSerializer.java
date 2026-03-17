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
package org.hyland.sdk.cic.http.client.mapper.jackson2.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;

/**
 * @since 1.0.0
 */
public class CICPrimitiveSerializer extends JsonSerializer<CICPrimitive<?>> {

    @Override
    public void serialize(CICPrimitive<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value instanceof CICPrimitive.CICBoolean bool) {
            gen.writeBoolean(bool.value());
        } else if (value instanceof CICPrimitive.CICInt integer) {
            gen.writeNumber(integer.value());
        } else if (value instanceof CICPrimitive.CICLong aLong) {
            gen.writeNumber(aLong.value());
        } else if (value instanceof CICPrimitive.CICString string) {
            gen.writeString(string.value());
        } else if (value instanceof CICPrimitive.CICDouble dbl) {
            gen.writeNumber(dbl.value());
        } else if (value instanceof CICPrimitive.CICNull) {
            gen.writeNull();
        } else {
            throw new IOException("Unsupported CICPrimitive type: " + value.getClass());
        }
    }
}
