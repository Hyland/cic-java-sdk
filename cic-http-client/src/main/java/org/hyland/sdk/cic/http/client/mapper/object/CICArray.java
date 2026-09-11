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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public interface CICArray extends CICNode {

    CICArray getArray(int index);

    boolean getBoolean(int index);

    int getInt(int index);

    long getLong(int index);

    CICObject getObject(int index);

    String getString(int index);

    double getDouble(int index);

    void addArray(CICArray value);

    void addBoolean(boolean value);

    void addInt(int value);

    void addLong(long value);

    void addObject(CICObject value);

    void addString(String value);

    void addDouble(double value);

    List<CICObject> toListObject();

    List<String> toListString();

    List<CICNode> getElements();

    static CICArray from(Object[] values) {
        var array = create();
        for (var value : values) {
            if (value instanceof Boolean b) {
                array.addBoolean(b);
            } else if (value instanceof Double i) {
                array.addDouble(i);
            } else if (value instanceof Integer i) {
                array.addInt(i);
            } else if (value instanceof Long l) {
                array.addLong(l);
            } else if (value instanceof String s) {
                array.addString(s);
            } else if (value instanceof Object[] objects) {
                array.addArray(from(objects));
            } else if (value instanceof boolean[] booleans) {
                array.addArray(from(booleans));
            } else if (value instanceof int[] ints) {
                array.addArray(from(ints));
            } else if (value instanceof long[] longs) {
                array.addArray(from(longs));
            } else if (value instanceof double[] doubles) {
                array.addArray(from(doubles));
            } else if (value != null && value.getClass().isArray()) {
                throw new CICSdkException(
                        "Unsupported array component type: %s".formatted(value.getClass().getComponentType()));
            } else if (value instanceof Collection<?> collection) {
                array.addArray(from(collection));
            } else if (value instanceof CICObject obj) {
                array.addObject(obj);
            } else if (value instanceof Map<?, ?> map) {
                // We have to assume the keys are strings since we can't represent non-string keys in JSON
                @SuppressWarnings("unchecked")
                var nestedObject = CICObject.from((Map<String, Object>) map);
                array.addObject(nestedObject);
            } else {
                throw new CICSdkException("Unsupported value type: %s".formatted(value));
            }
        }
        return array;
    }

    static CICArray from(Collection<?> collection) {
        return from(collection.toArray());
    }

    static CICArray from(boolean[] values) {
        var array = create();
        for (var value : values) {
            array.addBoolean(value);
        }
        return array;
    }

    static CICArray from(int[] values) {
        var array = create();
        for (var value : values) {
            array.addInt(value);
        }
        return array;
    }

    static CICArray from(long[] values) {
        var array = create();
        for (var value : values) {
            array.addLong(value);
        }
        return array;
    }

    static CICArray from(double[] values) {
        var array = create();
        for (var value : values) {
            array.addDouble(value);
        }
        return array;
    }

    /**
     * Returns a read-only view of the given array: mutating methods ({@code addXxx}) throw
     * {@link UnsupportedOperationException}, and nested arrays/objects are themselves returned as read-only views.
     *
     * @since 1.1.0
     */
    static CICArray unmodifiable(CICArray array) {
        return new CICArray() {

            @Override
            public CICArray getArray(int index) {
                return CICArray.unmodifiable(array.getArray(index));
            }

            @Override
            public boolean getBoolean(int index) {
                return array.getBoolean(index);
            }

            @Override
            public int getInt(int index) {
                return array.getInt(index);
            }

            @Override
            public long getLong(int index) {
                return array.getLong(index);
            }

            @Override
            public CICObject getObject(int index) {
                return CICObject.unmodifiable(array.getObject(index));
            }

            @Override
            public String getString(int index) {
                return array.getString(index);
            }

            @Override
            public double getDouble(int index) {
                return array.getDouble(index);
            }

            @Override
            public void addArray(CICArray value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addBoolean(boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addInt(int value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addLong(long value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addObject(CICObject value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addString(String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addDouble(double value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<CICObject> toListObject() {
                return array.toListObject().stream().map(CICObject::unmodifiable).toList();
            }

            @Override
            public List<String> toListString() {
                return array.toListString();
            }

            @Override
            public List<CICNode> getElements() {
                return array.getElements().stream().map(CICNode::unmodifiable).toList();
            }

            @Override
            public Object toJavaValue() {
                return array.toJavaValue();
            }

            @Override
            public boolean equals(Object o) {
                return array.equals(o);
            }

            @Override
            public int hashCode() {
                return array.hashCode();
            }

            @Override
            public String toString() {
                return array.toString();
            }
        };
    }

    static CICArray create() {
        var array = new ArrayList<CICNode>();
        return new CICArray() {

            @Override
            public CICArray getArray(int index) {
                return (CICArray) array.get(index);
            }

            @Override
            public boolean getBoolean(int index) {
                return ((CICPrimitive.CICBoolean) array.get(index)).value();
            }

            @Override
            public int getInt(int index) {
                return ((CICPrimitive.CICInt) array.get(index)).value();
            }

            @Override
            public long getLong(int index) {
                return ((CICPrimitive.CICLong) array.get(index)).value();
            }

            @Override
            public CICObject getObject(int index) {
                return (CICObject) array.get(index);
            }

            @Override
            public String getString(int index) {
                return ((CICPrimitive.CICString) array.get(index)).value();
            }

            @Override
            public void addArray(CICArray value) {
                array.add(value);
            }

            @Override
            public void addBoolean(boolean value) {
                array.add(new CICPrimitive.CICBoolean(value));
            }

            @Override
            public void addInt(int value) {
                array.add(new CICPrimitive.CICInt(value));
            }

            @Override
            public void addLong(long value) {
                array.add(new CICPrimitive.CICLong(value));
            }

            @Override
            public void addObject(CICObject value) {
                array.add(value);
            }

            @Override
            public void addString(String value) {
                array.add(new CICPrimitive.CICString(value));
            }

            @Override
            public double getDouble(int index) {
                return ((CICPrimitive.CICDouble) array.get(index)).value();
            }

            @Override
            public void addDouble(double value) {
                array.add(new CICPrimitive.CICDouble(value));
            }

            @Override
            @SuppressWarnings("unchecked")
            public List<CICObject> toListObject() {
                return (List<CICObject>) ((List<?>) List.copyOf(array));
            }

            @Override
            public List<String> toListString() {
                return array.stream().map(el -> ((CICPrimitive.CICString) el).value()).toList();
            }

            @Override
            public List<CICNode> getElements() {
                return List.copyOf(array);
            }

            @Override
            public Object toJavaValue() {
                return array.stream().map(CICNode::toJavaValue).toList();
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof CICArray other)) {
                    return false;
                }
                return Objects.equals(array, other.getElements());
            }

            @Override
            public int hashCode() {
                return array.hashCode();
            }

            @Override
            public String toString() {
                return array.toString();
            }
        };
    }
}
