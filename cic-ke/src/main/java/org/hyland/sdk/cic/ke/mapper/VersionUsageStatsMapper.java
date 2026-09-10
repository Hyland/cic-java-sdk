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

import java.util.Collections;
import java.util.LinkedHashMap;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.VersionUsageStats;

class VersionUsageStatsMapper implements CICMapper<VersionUsageStats> {

    @Override
    public VersionUsageStats fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var stats = new LinkedHashMap<String, Long>();
        for (var entry : cicObject.getProperties().entrySet()) {
            stats.put(entry.getKey(), cicObject.getLong(entry.getKey(), 0));
        }
        return new VersionUsageStats(Collections.unmodifiableMap(stats));
    }
}
