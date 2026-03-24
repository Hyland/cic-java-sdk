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

/**
 * @since 1.0.0
 */
public sealed interface CICPrimitive<T> extends CICNode {

    record CICBoolean(boolean value) implements CICPrimitive<Boolean> {

        @Override
        public Boolean getValue() {
            return value;
        }
    }

    record CICInt(int value) implements CICPrimitive<Integer> {

        @Override
        public Integer getValue() {
            return value;
        }
    }

    record CICLong(long value) implements CICPrimitive<Long> {

        @Override
        public Long getValue() {
            return value;
        }
    }

    record CICString(String value) implements CICPrimitive<String> {

        @Override
        public String getValue() {
            return value;
        }
    }

    record CICDouble(double value) implements CICPrimitive<Double> {
        @Override
        public Double getValue() {
            return value;
        }
    }

    record CICNull() implements CICPrimitive<Void> {
        @Override
        public Void getValue() {
            return null;
        }
    }

    T getValue();
}
