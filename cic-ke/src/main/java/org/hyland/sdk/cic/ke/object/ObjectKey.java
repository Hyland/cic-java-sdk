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

/**
 * Identifies content to process by either its object-storage path or platform document ID.
 *
 * @param path the object-storage path, or {@code null} when using a document ID
 * @param documentId the platform document ID, or {@code null} when using a path
 * @since 1.0.0
 */
public record ObjectKey(String path, String documentId) {

    public ObjectKey {
        var hasPath = path != null && !path.isBlank();
        var hasDocumentId = documentId != null && !documentId.isBlank();
        if (hasPath == hasDocumentId) {
            throw new IllegalArgumentException("Exactly one of path or documentId is required");
        }
    }

    public static ObjectKey forPath(String path) {
        return new ObjectKey(path, null);
    }

    public static ObjectKey forDocumentId(String documentId) {
        return new ObjectKey(null, documentId);
    }
}
