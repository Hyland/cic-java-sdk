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

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ke.object.ConfigOptions;
import org.hyland.sdk.cic.ke.object.ConfigRule;
import org.hyland.sdk.cic.ke.object.EmbeddingModel;
import org.hyland.sdk.cic.ke.object.EnrichmentResult;
import org.hyland.sdk.cic.ke.object.JobStatus;
import org.hyland.sdk.cic.ke.object.PresignResponse;
import org.hyland.sdk.cic.ke.object.PresignedUrl;
import org.hyland.sdk.cic.ke.object.ProcessRequest;
import org.hyland.sdk.cic.ke.object.ProcessResponse;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;
import org.hyland.sdk.cic.ke.object.RuleTestRequest;
import org.hyland.sdk.cic.ke.object.RuleTestResponse;

/**
 * @since 1.0.0
 */
public class KEMapperFactory implements MapperService.MapperFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        // Data Curation mappers
        if (PresignResponse.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new PresignResponseMapper();
        } else if (JobStatus.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new JobStatusMapper();
        } else if (EmbeddingModel.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new EmbeddingModelMapper();
        } else if (EmbeddingModel.ListOf.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new EmbeddingModelMapper.ListMapper();
        } else if (ProcessingOptions.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ProcessingOptionsMapper();
        }
        // Context API mappers
        else if (PresignedUrl.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new PresignedUrlMapper();
        } else if (ProcessRequest.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ProcessRequestMapper();
        } else if (ProcessResponse.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ProcessResponseMapper();
        } else if (EnrichmentResult.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new EnrichmentResultMapper();
        }
        // Configuration API mappers
        else if (ConfigOptions.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ConfigOptionsMapper();
        } else if (ConfigRule.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ConfigRuleMapper();
        } else if (ConfigRule.ListOf.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new ConfigRuleMapper.ListMapper();
        } else if (RuleTestRequest.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new RuleTestRequestMapper();
        } else if (RuleTestResponse.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new RuleTestResponseMapper();
        }
        return null;
    }
}
