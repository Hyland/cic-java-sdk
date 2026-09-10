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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ke.object.ActionDescriptor;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.HealthDetails;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;
import org.hyland.sdk.cic.ke.object.ProcessResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;
import org.hyland.sdk.cic.ke.object.VersionUsageStats;

/**
 * @since 1.1.0
 */
public class KEMapperFactory implements MapperService.MapperFactory {

    private static final List<Entry<Class<?>, CICMapper<?>>> MAPPERS = List.of(
            // Data Curation mappers
            Map.entry(PresignResponse.class, new PresignResponseMapper()),
            Map.entry(JobStatus.class, new JobStatusMapper()),
            Map.entry(EmbeddingModel.ListOf.class, new EmbeddingModelMapper.ListMapper()),
            Map.entry(EmbeddingModel.class, new EmbeddingModelMapper()),
            Map.entry(ProcessingOptions.class, new ProcessingOptionsMapper()),
            // Context API mappers
            Map.entry(ActionDescriptor.ListOf.class, new ActionDescriptorMapper.ListMapper()),
            Map.entry(ActionDescriptor.class, new ActionDescriptorMapper()),
            Map.entry(PresignedUrl.class, new PresignedUrlMapper()),
            Map.entry(ProcessRequest.class, new ProcessRequestMapper()),
            Map.entry(ProcessResponse.class, new ProcessResponseMapper()),
            Map.entry(EnrichmentResult.class, new EnrichmentResultMapper()),
            // Configuration API mappers
            Map.entry(ConfigOptions.class, new ConfigOptionsMapper()),
            Map.entry(ConfigRule.ListOf.class, new ConfigRuleMapper.ListMapper()),
            Map.entry(ConfigRule.class, new ConfigRuleMapper()),
            Map.entry(RuleTestRequest.class, new RuleTestRequestMapper()),
            Map.entry(RuleTestResponse.class, new RuleTestResponseMapper()),
            Map.entry(VersionUsageStats.class, new VersionUsageStatsMapper()),
            // Health
            Map.entry(HealthDetails.class, new HealthDetailsMapper()));

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        for (var entry : MAPPERS) {
            if (entry.getKey().isAssignableFrom(type)) {
                return (CICMapper<T>) entry.getValue();
            }
        }
        return null;
    }
}
