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
package org.hyland.sdk.cic.qna.object;

import java.util.Objects;

/**
 * @param id the unique conversation ID
 * @param name the conversation name, may be {@code null}
 * @param description the conversation description, may be {@code null}
 * @param lastModified the last modification timestamp as an ISO-8601 string (e.g. {@code "2026-04-02T11:42:00Z"}), may
 *            be {@code null}
 * @since 1.0.0
 */
public record Conversation(String id, String name, String description, String lastModified) {

    public Conversation {
        Objects.requireNonNull(id, "id cannot be null");
    }
}
