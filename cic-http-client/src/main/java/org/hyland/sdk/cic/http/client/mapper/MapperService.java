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
package org.hyland.sdk.cic.http.client.mapper;

import java.util.ServiceLoader;
import java.util.function.Function;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public class MapperService {

    private MapperService() {
        // utility class
    }

    public static <T> T read(String content, Class<T> type) {
        // prepare mapper
        var serializer = loadSerializer();
        var mapper = loadMapper(type);
        // read and convert
        var cicNode = serializer.read(content);
        return mapper.fromCICNode(cicNode);
    }

    @SuppressWarnings("unchecked")
    public static <T> String writeAsString(T object) {
        // prepare mapper
        var mapper = loadMapper((Class<T>) object.getClass());
        var serializer = loadSerializer();
        // convert and write
        var cicNode = mapper.toCICNode(object);
        return serializer.writeAsString(cicNode);
    }

    protected static CICSerializer loadSerializer() {
        for (var factory : ServiceLoader.load(SerializerFactory.class)) {
            var reader = factory.getSerializer();
            if (reader != null) {
                return reader;
            }
        }
        throw new CICSdkException("No CICReader found, check the classpath for implementations of SerializerFactory");
    }

    protected static <T> CICMapper<T> loadMapper(Class<T> type) {
        for (var factory : ServiceLoader.load(MapperFactory.class)) {
            var mapper = factory.getMapper(type);
            if (mapper != null) {
                return mapper;
            }
        }
        throw new CICSdkException("No CICMapper found for the type: " + type.getName()
                + ", check the classpath for implementations of MapperFactory");
    }

    protected static <T> Function<CICObject, T> loadFromMethod(Class<T> type) {
        try {
            var method = type.getDeclaredMethod("from", CICObject.class);
            return cicObject -> {
                try {
                    return type.cast(method.invoke(null, cicObject));
                } catch (ReflectiveOperationException e) {
                    throw new CICSdkException("Unable to invoke method: from for the type: " + type.getName(), e);
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new CICSdkException("Unable to retrieve method: from for the type: " + type.getName(), e);
        }
    }

    public interface SerializerFactory {

        CICSerializer getSerializer();
    }

    public interface MapperFactory {

        <T> CICMapper<T> getMapper(Class<T> type);
    }
}
