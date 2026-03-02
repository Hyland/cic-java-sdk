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
package org.hyland.sdk.cic.http.client.auth.mapper;

import static org.hyland.sdk.cic.http.client.util.MapMultis.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import org.junit.jupiter.api.Test;

import org.hyland.sdk.cic.http.client.mapper.MapperService;

/**
 * @since 1.0.0
 */
public class AuthenticationMapperFactoryTest {

    @Test
    public void getWithServiceLoader() {
        var serializerFactory = ServiceLoader.load(MapperService.MapperFactory.class)
                                             .stream()
                                             .map(Provider::get)
                                             .mapMulti(instanceOf(AuthenticationMapperFactory.class))
                                             .findFirst();
        assertTrue(serializerFactory.isPresent(), "AuthenticationMapperFactory should be found by ServiceLoader");
    }
}
