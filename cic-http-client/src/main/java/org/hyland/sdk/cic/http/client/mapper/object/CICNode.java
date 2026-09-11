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

import java.util.Collection;
import java.util.Map;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public interface CICNode {

    Object toJavaValue();

    static CICNode from(Object value) {
        if (value == null) {
            return new CICPrimitive.CICNull();
        } else if (value instanceof Object[] objects) {
            return CICArray.from(objects);
        } else if (value instanceof boolean[] booleans) {
            return CICArray.from(booleans);
        } else if (value instanceof int[] ints) {
            return CICArray.from(ints);
        } else if (value instanceof long[] longs) {
            return CICArray.from(longs);
        } else if (value instanceof double[] doubles) {
            return CICArray.from(doubles);
        } else if (value.getClass().isArray()) {
            throw new CICSdkException(
                    "Unsupported array component type: %s".formatted(value.getClass().getComponentType()));
        } else if (value instanceof Collection<?> collection) {
            return CICArray.from(collection);
        } else if (value instanceof Map<?, ?> map) {
            // We have to assume the keys are strings since we can't represent non-string keys in JSON
            @SuppressWarnings("unchecked")
            var nestedObject = CICObject.from((Map<String, Object>) map);
            return nestedObject;
        } else if (value instanceof Boolean b) {
            return new CICPrimitive.CICBoolean(b);
        } else if (value instanceof Double d) {
            return new CICPrimitive.CICDouble(d);
        } else if (value instanceof Integer i) {
            return new CICPrimitive.CICInt(i);
        } else if (value instanceof Long l) {
            return new CICPrimitive.CICLong(l);
        } else if (value instanceof String s) {
            return new CICPrimitive.CICString(s);
        } else {
            throw new CICSdkException("Unsupported value type: %s".formatted(value));
        }
    }

    /**
     * Returns a read-only view of the given node: {@link CICArray}/{@link CICObject} are mutable, so this guards
     * against a caller mutating a node it doesn't own (e.g. one retained by an otherwise immutable object). Primitives
     * (and {@code null}) are already immutable and returned as-is.
     *
     * @since 1.1.0
     */
    static CICNode unmodifiable(CICNode node) {
        if (node instanceof CICArray array) {
            return CICArray.unmodifiable(array);
        } else if (node instanceof CICObject object) {
            return CICObject.unmodifiable(object);
        }
        return node;
    }
}
