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

import java.time.Duration;

/**
 * Detailed health information from the Data Curation service.
 *
 * @param status overall service status (e.g. "healthy", "degraded")
 * @param timestamp the health check timestamp
 * @param application application-level details (version, uptime)
 * @param system system-level resource usage (CPU, memory, disk)
 * @param awsOk whether the AWS dependency check passed
 * @since 1.1.0
 */
public record HealthDetails(String status, String timestamp, Application application, System system, boolean awsOk) {

    /**
     * Application-level health details.
     *
     * @param version the application version
     * @param uptime the application uptime
     */
    public record Application(String version, Duration uptime) {
    }

    /**
     * System-level resource usage.
     *
     * @param cpuPercent CPU usage percentage
     * @param memoryUsedPercent memory usage percentage
     * @param diskUsedPercent disk usage percentage
     */
    public record System(Percentage cpuPercent, Percentage memoryUsedPercent, Percentage diskUsedPercent) {
    }
}
