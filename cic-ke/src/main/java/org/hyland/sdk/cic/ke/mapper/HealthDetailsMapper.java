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
package org.hyland.sdk.cic.ke.mapper;

import java.time.Duration;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.HealthDetails;
import org.hyland.sdk.cic.ke.object.Percentage;

class HealthDetailsMapper implements CICMapper<HealthDetails> {

    @Override
    public HealthDetails fromCICNode(CICNode cicNode) {
        var obj = (CICObject) cicNode;
        var status = obj.getStringOrNull("status");
        var timestamp = obj.getStringOrNull("timestamp");
        var application = mapApplication(obj);
        var system = mapSystem(obj);
        var awsOk = obj.getOptionalObject("checks")
                       .flatMap(c -> c.getOptionalObject("aws"))
                       .map(a -> a.getBoolean("ok", false))
                       .orElse(false);

        return new HealthDetails(status, timestamp, application, system, awsOk);
    }

    private HealthDetails.Application mapApplication(CICObject obj) {
        return obj.getOptionalObject("application").map(app -> {
            var version = app.getStringOrNull("version");
            var uptimeSeconds = (long) app.getDouble("uptime_seconds", 0.0);
            return new HealthDetails.Application(version, Duration.ofSeconds(uptimeSeconds));
        }).orElse(null);
    }

    private HealthDetails.System mapSystem(CICObject obj) {
        return obj.getOptionalObject("system")
                  .map(sys -> new HealthDetails.System(new Percentage(sys.getDouble("cpu_percent", 0.0)),
                          new Percentage(sys.getDouble("memory_used_percent", 0.0)),
                          new Percentage(sys.getDouble("disk_used_percent", 0.0))))
                  .orElse(null);
    }
}
