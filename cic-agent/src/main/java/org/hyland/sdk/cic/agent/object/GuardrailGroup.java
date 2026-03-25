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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @since 1.0.0
 */
public record GuardrailGroup(String displayName, String description, List<GuardrailDefinition> guardrails) {

    public GuardrailGroup {
        Objects.requireNonNull(displayName, "displayName cannot be null");
        Objects.requireNonNull(description, "description cannot be null");
        guardrails = guardrails != null ? Collections.unmodifiableList(guardrails) : List.of();
    }
}
