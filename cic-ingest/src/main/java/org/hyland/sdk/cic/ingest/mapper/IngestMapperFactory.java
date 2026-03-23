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
 *     Kevin Leturc <kevin.leturc@hyland.com>
 */
package org.hyland.sdk.cic.ingest.mapper;

import org.hyland.sdk.cic.http.client.mapper.CICMapper;
import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

/**
 * @since 1.0.0
 */
public class IngestMapperFactory implements MapperService.MapperFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> CICMapper<T> getMapper(Class<T> type) {
        if (IngestEvent.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new IngestEventMapper();
        } else if (IngestEvent.Batch.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new IngestEventMapper.BatchMapper();
        } else if (PreSignedUrl.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new PreSignedUrlMapper();
        } else if (PreSignedUrl.List.class.isAssignableFrom(type)) {
            return (CICMapper<T>) new PreSignedUrlMapper.ListMapper();
        }
        return null;
    }
}
