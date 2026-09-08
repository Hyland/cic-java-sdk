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
 * Response from {@code GET /status/{job_id}} endpoint.
 * <p>
 * The status field follows the lifecycle: PENDING -> PROCESSING/IN_PROGRESS -> COMPLETED/FAILED.
 * <p>
 * A COMPLETED status does not always mean success — the response payload may contain an error. Use {@link #hasError()}
 * to detect this case. A FAILED status means the pipeline exhausted all retries.
 *
 * @since 1.1.0
 */
public record JobStatus(String jobId, String status, String errorMessage) {

    /**
     * Convenience constructor for responses without an error message.
     */
    public JobStatus(String jobId, String status) {
        this(jobId, status, null);
    }

    /**
     * @return true when the job completed successfully (COMPLETED/DONE with no error payload)
     */
    public boolean isDone() {
        return isCompleted() && !hasError();
    }

    /**
     * @return true when the status is COMPLETED or DONE (does not imply success — check {@link #hasError()})
     */
    public boolean isCompleted() {
        return "DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status);
    }

    /**
     * @return true when the pipeline reached a terminal failure after exhausting retries
     */
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }

    /**
     * @return true when the job has reached a terminal state (COMPLETED or FAILED) and will not change further
     */
    public boolean isTerminal() {
        return isCompleted() || isFailed();
    }

    /**
     * @return true when the response contains an error message (possible even with COMPLETED status)
     */
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
