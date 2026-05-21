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
package org.hyland.sdk.cic.nucleus.mapper;

import java.util.stream.Collectors;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.nucleus.object.Attribute;

/**
 * @since 1.0.0
 */
class AttributeMapper implements CICMapper<Attribute> {

    @Override
    public Attribute fromCICNode(CICNode cicNode) {
        if (!(cicNode instanceof CICObject obj)) {
            throw new IllegalArgumentException("Expected CICObject, got: " + cicNode.getClass().getSimpleName());
        }
        return new Attribute(obj.getStringOrThrow("key"),
                obj.getOptionalArray("values").map(CICArray::toListString).orElse(null));
    }

    @Override
    public CICNode toCICNode(Attribute attribute) {
        var obj = CICObject.create();
        obj.putString("key", attribute.key());
        obj.putArray("values", CICArray.from(attribute.values()));
        return obj;
    }

    static class ListMapper implements CICMapper<Attribute.ListOf> {

        private final AttributeMapper innerMapper = new AttributeMapper();

        @Override
        public Attribute.ListOf fromCICNode(CICNode cicNode) {
            if (!(cicNode instanceof CICArray cicArray)) {
                throw new IllegalArgumentException("Expected CICArray, got: " + cicNode.getClass().getSimpleName());
            }
            return cicArray.toListObject()
                           .stream()
                           .map(innerMapper::fromCICNode)
                           .collect(Collectors.toCollection(Attribute.ListOf::new));
        }
    }
}
