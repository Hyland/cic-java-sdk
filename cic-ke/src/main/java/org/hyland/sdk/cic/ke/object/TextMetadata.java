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
 *     Abhishek Gupta
 */
package org.hyland.sdk.cic.ke.object;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents text metadata extracted by the Context API {@code textMetadataGeneration} action.
 * <p>
 * The metadata keys are dynamic and depend on the document content (e.g. author, title, language).
 *
 * @param properties the metadata key-value pairs
 * @since 1.1.0
 */
public record TextMetadata(Map<String, Object> properties) implements EnrichmentData {

    public TextMetadata {
        properties = properties == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    /**
     * Returns the metadata value for the given key, or {@code null} if not present.
     */
    public Object get(String key) {
        return properties.get(key);
    }
}
