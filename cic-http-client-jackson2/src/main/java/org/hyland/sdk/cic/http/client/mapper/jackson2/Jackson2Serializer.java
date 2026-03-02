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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.CICSerializer;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;

/**
 * @since 1.0.0
 */
public class Jackson2Serializer implements CICSerializer {

    protected final ObjectMapper objectMapper;

    public Jackson2Serializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CICNode read(String plainText) {
        try {
            return objectMapper.readerFor(CICNode.class).readValue(plainText);
        } catch (JsonProcessingException e) {
            throw new CICSdkException("Unable to read json", e);
        }
    }

    @Override
    public String writeAsString(CICNode node) {
        try {
            return objectMapper.writerFor(CICNode.class).writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new CICSdkException("Unable to write json", e);
        }
    }
}
