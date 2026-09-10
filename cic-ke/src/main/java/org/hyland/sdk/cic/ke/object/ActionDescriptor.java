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

import java.util.ArrayList;
import java.util.List;

/**
 * Describes an available action from the {@code GET /content/process/actions} v2 endpoint, including any available
 * models and categories for actions that support them.
 *
 * @param name the action name (e.g. "pretrainedClassification", "textSummarization")
 * @param availableModels models available for this action (empty if not applicable)
 * @param availableCategories categories available for this action (empty if not applicable)
 * @since 1.1.0
 */
public record ActionDescriptor(String name, List<String> availableModels, List<String> availableCategories) {

    public ActionDescriptor {
        availableModels = availableModels == null ? List.of() : List.copyOf(availableModels);
        availableCategories = availableCategories == null ? List.of() : List.copyOf(availableCategories);
    }

    /**
     * @since 1.1.0
     */
    public static class ListOf extends ArrayList<ActionDescriptor> {
    }
}
