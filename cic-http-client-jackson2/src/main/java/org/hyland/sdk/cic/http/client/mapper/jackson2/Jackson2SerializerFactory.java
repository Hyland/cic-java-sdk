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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.hyland.sdk.cic.http.client.mapper.MapperService.SerializerFactory;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICArrayDeserializer;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICArraySerializer;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICNodeDeserializer;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICObjectDeserializer;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICObjectSerializer;
import org.hyland.sdk.cic.http.client.mapper.jackson2.serializer.CICPrimitiveSerializer;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;

/**
 * @since 1.0.0
 */
public class Jackson2SerializerFactory implements SerializerFactory {

    protected static final Jackson2Serializer SERIALIZER;

    static {
        var module = new SimpleModule();
        module.addDeserializer(CICNode.class, new CICNodeDeserializer());
        module.addDeserializer(CICObject.class, new CICObjectDeserializer());
        module.addDeserializer(CICArray.class, new CICArrayDeserializer());
        module.addSerializer(CICObject.class, new CICObjectSerializer());
        module.addSerializer(CICArray.class, new CICArraySerializer());
        module.addSerializer(CICPrimitive.CICBoolean.class, new CICPrimitiveSerializer());
        module.addSerializer(CICPrimitive.CICInt.class, new CICPrimitiveSerializer());
        module.addSerializer(CICPrimitive.CICLong.class, new CICPrimitiveSerializer());
        module.addSerializer(CICPrimitive.CICString.class, new CICPrimitiveSerializer());
        module.addSerializer(CICPrimitive.CICDouble.class, new CICPrimitiveSerializer());
        module.addSerializer(CICPrimitive.CICNull.class, new CICPrimitiveSerializer());

        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);

        SERIALIZER = new Jackson2Serializer(objectMapper);
    }

    @Override
    public Jackson2Serializer getSerializer() {
        return SERIALIZER;
    }
}
