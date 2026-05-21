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
package org.hyland.sdk.cic.nucleus.object;

import java.util.Objects;

/**
 * @since 1.0.0
 */
public enum SystemIntegrationType {

    ON_BASE("OnBase"), ALFRESCO("Alfresco"), PERCEPTIVE("Perceptive"), NUXEO("Nuxeo"), CONFLUENCE(
            "Confluence"), SHARE_POINT("SharePoint"), LEGACY_HXP_R("LegacyHxPR"), LOCAL("Local");

    private final String value;

    SystemIntegrationType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static SystemIntegrationType fromValue(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        for (var type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown SystemIntegrationType: " + value);
    }
}
