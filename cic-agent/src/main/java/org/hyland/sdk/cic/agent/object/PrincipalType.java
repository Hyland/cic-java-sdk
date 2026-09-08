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
package org.hyland.sdk.cic.agent.object;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @since 1.0.0
 */
public enum PrincipalType {

    USER("User"),
    GROUP("Group");

    private static final Map<String, PrincipalType> BY_VALUE = Arrays.stream(
            values()).collect(Collectors.toUnmodifiableMap(PrincipalType::value, t -> t));

    private final String value;

    PrincipalType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PrincipalType fromValue(String value) {
        var result = BY_VALUE.get(value);
        if (result == null) {
            throw new IllegalArgumentException("Unknown PrincipalType: " + value);
        }
        return result;
    }
}
