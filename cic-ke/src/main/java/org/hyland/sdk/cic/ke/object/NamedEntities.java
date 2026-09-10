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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents named entities extracted by the Context API NER actions ({@code namedEntityRecognitionText} and
 * {@code namedEntityRecognitionImage}).
 * <p>
 * Entities are grouped by category (e.g. "PERSON", "ORG", "LOCATION"). Each category maps to a list of recognized
 * entity values.
 *
 * @param entities the entity categories and their recognized values
 * @since 1.1.0
 */
public record NamedEntities(Map<String, List<String>> entities) implements EnrichmentData {

    public NamedEntities {
        entities = entities == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(entities));
    }

    /**
     * Returns the recognized entity values for the given category, or an empty list if the category is not present.
     */
    public List<String> get(String category) {
        return entities.getOrDefault(category, List.of());
    }

    /**
     * Returns the set of recognized entity categories.
     */
    public Set<String> categories() {
        return entities.keySet();
    }
}
