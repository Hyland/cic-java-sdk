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

import java.util.LinkedHashMap;

import org.hyland.sdk.cic.http.client.CICSdkException;

/**
 * @since 1.0.0
 */
public interface CICObject extends CICNode {

    boolean isEmpty();

    boolean getBoolean(String key, boolean defaultValue);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    String getString(String key, String defaultValue);

    CICArray getArrayOrThrow(String key);

    boolean getBooleanOrThrow(String key);

    int getIntOrThrow(String key);

    long getLongOrThrow(String key);

    CICObject getObjectOrThrow(String key);

    String getStringOrThrow(String key);

    void putArray(String key, CICArray value);

    void putBoolean(String key, boolean value);

    void putInt(String key, int value);

    void putLong(String key, long value);

    void putObject(String key, CICObject value);

    void putString(String key, String value);

    static CICObject create() {
        var properties = new LinkedHashMap<String, CICNode>();
        return new CICObject() {

            @Override
            public boolean isEmpty() {
                return properties.isEmpty();
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
            public void putObject(String key, CICObject value) {
                properties.put(key, value);
            }

            @Override
            public void putString(String key, String value) {
                properties.put(key, new CICPrimitive.CICString(value));
            }

        };
    }
}
