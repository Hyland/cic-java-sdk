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
            } else if (v.getClass().isArray()) {
                cicObject.putArray(k, CICArray.from((Object[]) v));
            } else if (v instanceof Collection<?> collection) {
                cicObject.putArray(k, CICArray.from(collection.toArray()));
            } else {
                throw new CICSdkException("Unsupported value type: %s for key: %s".formatted(v.getClass(), k));
            }
        });
        return cicObject;
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
        };
    }
}
