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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hyland.sdk.cic.http.client.CICSdkException;
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive.CICDouble;

/**
 * @since 1.0.0
 */
public interface CICObject extends CICNode {

    boolean isEmpty();

    Map<String, CICNode> getProperties();

    boolean getBoolean(String key, boolean defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    String getString(String key, String defaultValue);

    LocalDate getLocalDate(String key, LocalDate defaultValue);

    CICArray getArrayOrThrow(String key);

    boolean getBooleanOrThrow(String key);

    int getIntOrThrow(String key);

    long getLongOrThrow(String key);

    CICObject getObjectOrThrow(String key);

    String getStringOrThrow(String key);

    LocalDate getLocalDateOrThrow(String key);

    Optional<CICObject> getOptionalObject(String key);

    Optional<CICArray> getOptionalArray(String key);

    Optional<String> getOptionalString(String key);

    String getStringOrNull(String key);

    Integer getIntegerOrNull(String key);

    Boolean getBooleanOrNull(String key);

    Double getDoubleOrNull(String key);

    Map<String, Object> toMap();

    void putArray(String key, CICArray value);

    void putBoolean(String key, boolean value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putNode(String key, CICNode value);

    void putObject(String key, CICObject value);

    void putString(String key, String value);

    void putDouble(String key, double value);

    void putLocalDate(String key, LocalDate value);

    void putNull(String key);

    double getDouble(String key, double defaultValue);

    static CICObject from(Map<String, ?> value) {
        var cicObject = create();
        value.forEach((k, v) -> {
            if (v instanceof Boolean bool) {
                cicObject.putBoolean(k, bool);
            } else if (v instanceof Double dbl) {
                cicObject.putDouble(k, dbl);
            } else if (v instanceof Integer integer) {
                cicObject.putInt(k, integer);
            } else if (v instanceof Long aLong) {
                cicObject.putLong(k, aLong);
            } else if (v instanceof String str) {
                cicObject.putString(k, str);
            } else if (v instanceof Map<?, ?> map) {
                // We have to assume the keys are strings since we can't represent non-string keys in JSON
                @SuppressWarnings("unchecked")
                var nestedObject = from((Map<String, Object>) map);
                cicObject.putObject(k, nestedObject);
            } else if (v == null) {
                cicObject.putNull(k);
            } else if (v instanceof Object[] objects) {
                cicObject.putArray(k, CICArray.from(objects));
            } else if (v instanceof boolean[] booleans) {
                cicObject.putArray(k, CICArray.from(booleans));
            } else if (v instanceof int[] ints) {
                cicObject.putArray(k, CICArray.from(ints));
            } else if (v instanceof long[] longs) {
                cicObject.putArray(k, CICArray.from(longs));
            } else if (v instanceof double[] doubles) {
                cicObject.putArray(k, CICArray.from(doubles));
            } else if (v.getClass().isArray()) {
                throw new CICSdkException("Unsupported array component type: %s for key: %s".formatted(
                        v.getClass().getComponentType(), k));
            } else if (v instanceof Collection<?> collection) {
                cicObject.putArray(k, CICArray.from(collection.toArray()));
            } else {
                throw new CICSdkException("Unsupported value type: %s for key: %s".formatted(v.getClass(), k));
            }
        });
        return cicObject;
    }

    /**
     * Returns a read-only view of the given object: mutating methods ({@code putXxx}) throw
     * {@link UnsupportedOperationException}, and nested arrays/objects are themselves returned as read-only views.
     *
     * @since 1.1.0
     */
    static CICObject unmodifiable(CICObject object) {
        return new CICObject() {

            @Override
            public boolean isEmpty() {
                return object.isEmpty();
            }

            @Override
            public Map<String, CICNode> getProperties() {
                var properties = new LinkedHashMap<String, CICNode>();
                object.getProperties().forEach((k, v) -> properties.put(k, CICNode.unmodifiable(v)));
                return Collections.unmodifiableMap(properties);
            }

            @Override
            public boolean getBoolean(String key, boolean defaultValue) {
                return object.getBoolean(key, defaultValue);
            }

            @Override
            public int getInt(String key, int defaultValue) {
                return object.getInt(key, defaultValue);
            }

            @Override
            public long getLong(String key, long defaultValue) {
                return object.getLong(key, defaultValue);
            }

            @Override
            public String getString(String key, String defaultValue) {
                return object.getString(key, defaultValue);
            }

            @Override
            public LocalDate getLocalDate(String key, LocalDate defaultValue) {
                return object.getLocalDate(key, defaultValue);
            }

            @Override
            public CICArray getArrayOrThrow(String key) {
                return CICArray.unmodifiable(object.getArrayOrThrow(key));
            }

            @Override
            public boolean getBooleanOrThrow(String key) {
                return object.getBooleanOrThrow(key);
            }

            @Override
            public int getIntOrThrow(String key) {
                return object.getIntOrThrow(key);
            }

            @Override
            public long getLongOrThrow(String key) {
                return object.getLongOrThrow(key);
            }

            @Override
            public CICObject getObjectOrThrow(String key) {
                return CICObject.unmodifiable(object.getObjectOrThrow(key));
            }

            @Override
            public String getStringOrThrow(String key) {
                return object.getStringOrThrow(key);
            }

            @Override
            public LocalDate getLocalDateOrThrow(String key) {
                return object.getLocalDateOrThrow(key);
            }

            @Override
            public Optional<CICObject> getOptionalObject(String key) {
                return object.getOptionalObject(key).map(CICObject::unmodifiable);
            }

            @Override
            public Optional<CICArray> getOptionalArray(String key) {
                return object.getOptionalArray(key).map(CICArray::unmodifiable);
            }

            @Override
            public Optional<String> getOptionalString(String key) {
                return object.getOptionalString(key);
            }

            @Override
            public String getStringOrNull(String key) {
                return object.getStringOrNull(key);
            }

            @Override
            public Integer getIntegerOrNull(String key) {
                return object.getIntegerOrNull(key);
            }

            @Override
            public Boolean getBooleanOrNull(String key) {
                return object.getBooleanOrNull(key);
            }

            @Override
            public Double getDoubleOrNull(String key) {
                return object.getDoubleOrNull(key);
            }

            @Override
            public Map<String, Object> toMap() {
                return object.toMap();
            }

            @Override
            public void putArray(String key, CICArray value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putBoolean(String key, boolean value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putInt(String key, int value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putLong(String key, long value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putNode(String key, CICNode value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putObject(String key, CICObject value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putString(String key, String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putDouble(String key, double value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putLocalDate(String key, LocalDate value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void putNull(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public double getDouble(String key, double defaultValue) {
                return object.getDouble(key, defaultValue);
            }

            @Override
            public Object toJavaValue() {
                return object.toJavaValue();
            }

            @Override
            public boolean equals(Object o) {
                return object.equals(o);
            }

            @Override
            public int hashCode() {
                return object.hashCode();
            }

            @Override
            public String toString() {
                return object.toString();
            }
        };
    }

    static CICObject create() {
        var properties = new LinkedHashMap<String, CICNode>();
        return new CICObject() {

            @Override
            public boolean isEmpty() {
                return properties.isEmpty();
            }

            @Override
            public Map<String, CICNode> getProperties() {
                return Collections.unmodifiableMap(properties);
            }

            @Override
            public boolean getBoolean(String key, boolean defaultValue) {
                if (!properties.containsKey(key)) {
                    return defaultValue;
                } else if (properties.get(key) instanceof CICPrimitive.CICBoolean bool) {
                    return bool.value();
                }
                throw new CICSdkException("Property: %s is not a boolean".formatted(key));
            }

            @Override
            public int getInt(String key, int defaultValue) {
                if (!properties.containsKey(key)) {
                    return defaultValue;
                } else {
                    return getIntOrThrow(key);
                }
            }

            @Override
            public long getLong(String key, long defaultValue) {
                if (!properties.containsKey(key)) {
                    return defaultValue;
                } else {
                    return getLongOrThrow(key);
                }
            }

            @Override
            public String getString(String key, String defaultValue) {
                if (!properties.containsKey(key)) {
                    return defaultValue;
                } else if (properties.get(key) instanceof CICPrimitive.CICString string) {
                    return string.value();
                }
                throw new CICSdkException("Property: %s is not a string".formatted(key));
            }

            @Override
            public LocalDate getLocalDate(String key, LocalDate defaultValue) {
                var value = getString(key, null);
                if (value == null) {
                    return defaultValue;
                }
                return LocalDate.parse(value);
            }

            @Override
            public CICArray getArrayOrThrow(String key) {
                if (properties.get(key) instanceof CICArray array) {
                    return array;
                }
                throw new CICSdkException("Property: %s does not exist or is not an array".formatted(key));
            }

            @Override
            public boolean getBooleanOrThrow(String key) {
                if (properties.get(key) instanceof CICPrimitive.CICBoolean bool) {
                    return bool.value();
                }
                throw new CICSdkException("Property: %s does not exist or is not a boolean".formatted(key));
            }

            @Override
            public int getIntOrThrow(String key) {
                if (properties.get(key) instanceof CICPrimitive.CICInt integer) {
                    return integer.value();
                } else if (properties.get(key) instanceof CICPrimitive.CICLong aLong) {
                    try {
                        return Math.toIntExact(aLong.value());
                    } catch (ArithmeticException e) {
                        throw new CICSdkException("Property: %s is too large to fit in an int".formatted(key), e);
                    }
                } else if (properties.containsKey(key)) {
                    throw new CICSdkException("Property: %s is not a number".formatted(key));
                } else {
                    throw new CICSdkException("Property: %s does not exist".formatted(key));
                }
            }

            @Override
            public long getLongOrThrow(String key) {
                if (properties.get(key) instanceof CICPrimitive.CICInt integer) {
                    return integer.value();
                } else if (properties.get(key) instanceof CICPrimitive.CICLong aLong) {
                    return aLong.value();
                } else if (properties.containsKey(key)) {
                    throw new CICSdkException("Property: %s is not a number".formatted(key));
                } else {
                    throw new CICSdkException("Property: %s does not exist".formatted(key));
                }
            }

            @Override
            public CICObject getObjectOrThrow(String key) {
                if (properties.get(key) instanceof CICObject object) {
                    return object;
                }
                throw new CICSdkException("Property: %s does not exist or is not an object".formatted(key));
            }

            @Override
            public String getStringOrThrow(String key) {
                String value = getString(key, null);
                if (value != null) {
                    return value;
                }
                throw new CICSdkException("Property: %s does not exist or is not a string".formatted(key));
            }

            @Override
            public LocalDate getLocalDateOrThrow(String key) {
                LocalDate value = getLocalDate(key, null);
                if (value != null) {
                    return value;
                }
                throw new CICSdkException("Property: %s does not exist or is not a date".formatted(key));
            }

            @Override
            public Optional<CICObject> getOptionalObject(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return Optional.empty();
                }
                return Optional.of(getObjectOrThrow(key));
            }

            @Override
            public Optional<CICArray> getOptionalArray(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return Optional.empty();
                }
                return Optional.of(getArrayOrThrow(key));
            }

            @Override
            public Optional<String> getOptionalString(String key) {
                return Optional.ofNullable(getStringOrNull(key));
            }

            @Override
            public String getStringOrNull(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return null;
                }
                return getString(key, null);
            }

            @Override
            public Integer getIntegerOrNull(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return null;
                }
                return getInt(key, 0);
            }

            @Override
            public Boolean getBooleanOrNull(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return null;
                }
                return getBoolean(key, false);
            }

            @Override
            public Double getDoubleOrNull(String key) {
                var node = properties.get(key);
                if (node == null || node instanceof CICPrimitive.CICNull) {
                    return null;
                }
                return getDouble(key, 0.0);
            }

            @Override
            public Map<String, Object> toMap() {
                var map = new LinkedHashMap<String, Object>();
                properties.forEach((k, v) -> map.put(k, v.toJavaValue()));
                return map;
            }

            @Override
            public Object toJavaValue() {
                return toMap();
            }

            @Override
            public double getDouble(String key, double defaultValue) {
                if (!properties.containsKey(key)) {
                    return defaultValue;
                } else if (properties.get(key) instanceof CICPrimitive.CICDouble dbl) {
                    return dbl.value();
                }
                throw new CICSdkException("Property: %s is not a double".formatted(key));
            }

            @Override
            public void putArray(String key, CICArray value) {
                properties.put(key, value);
            }

            @Override
            public void putBoolean(String key, boolean value) {
                properties.put(key, new CICPrimitive.CICBoolean(value));
            }

            @Override
            public void putInt(String key, int value) {
                properties.put(key, new CICPrimitive.CICInt(value));
            }

            @Override
            public void putLong(String key, long value) {
                properties.put(key, new CICPrimitive.CICLong(value));
            }

            @Override
            public void putNode(String key, CICNode value) {
                properties.put(key, value);
            }

            @Override
            public void putObject(String key, CICObject value) {
                properties.put(key, value);
            }

            @Override
            public void putString(String key, String value) {
                properties.put(key, new CICPrimitive.CICString(value));
            }

            @Override
            public void putLocalDate(String key, LocalDate value) {
                properties.put(key, new CICPrimitive.CICString(value.toString()));
            }

            @Override
            public void putDouble(String key, double value) {
                properties.put(key, new CICDouble(value));
            }

            @Override
            public void putNull(String key) {
                properties.put(key, new CICPrimitive.CICNull());
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof CICObject other)) {
                    return false;
                }
                return Objects.equals(properties, other.getProperties());
            }

            @Override
            public int hashCode() {
                return properties.hashCode();
            }

            @Override
            public String toString() {
                return properties.toString();
            }
        };
    }
}
