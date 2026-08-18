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
import org.hyland.sdk.cic.http.client.mapper.object.CICNode;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;
import org.hyland.sdk.cic.ke.object.NormalizationOptions;
import org.hyland.sdk.cic.ke.object.PiiOptions;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;

/**
 * @since 1.0.0
 */
class ProcessingOptionsMapper implements CICMapper<ProcessingOptions> {

    @Override
    public ProcessingOptions fromCICNode(CICNode cicNode) {
        var cicObject = (CICObject) cicNode;
        var builder = ProcessingOptions.builder();

        cicObject.getOptionalObject("normalization").ifPresent(norm -> {
            var normBuilder = NormalizationOptions.builder();
            var quotations = norm.getBooleanOrNull("quotations");
            if (quotations != null) {
                normBuilder.quotations(quotations);
            }
            var dashes = norm.getBooleanOrNull("dashes");
            if (dashes != null) {
                normBuilder.dashes(dashes);
            }
            builder.normalization(normBuilder.build());
        });

        var chunking = cicObject.getBooleanOrNull("chunking");
        if (chunking != null) {
            builder.chunking(chunking);
        }

        cicObject.getOptionalString("chunking_strategy").ifPresent(builder::chunkingStrategy);

        var chunkSize = cicObject.getIntegerOrNull("chunk_size");
        if (chunkSize != null) {
            builder.chunkSize(chunkSize);
        }

        var embedding = cicObject.getBooleanOrNull("embedding");
        if (embedding != null) {
            builder.embedding(embedding);
        }

        cicObject.getOptionalString("embeddings_model").ifPresent(builder::embeddingsModel);

        cicObject.getOptionalString("json_schema").ifPresent(builder::jsonSchema);

        cicObject.getOptionalObject("pii").ifPresent(piiObj -> {
            var piiBuilder = PiiOptions.builder();
            piiObj.getOptionalString("mode").ifPresent(piiBuilder::mode);
            var entityRedaction = piiObj.getBooleanOrNull("entity_redaction");
            if (entityRedaction != null) {
                piiBuilder.entityRedaction(entityRedaction);
            }
            builder.pii(piiBuilder.build());
        });

        return builder.build();
    }

    @Override
    public CICNode toCICNode(ProcessingOptions options) {
        var cicObject = CICObject.create();

        if (options.normalization() != null) {
            var norm = CICObject.create();
            if (options.normalization().quotations() != null) {
                norm.putBoolean("quotations", options.normalization().quotations());
            }
            if (options.normalization().dashes() != null) {
                norm.putBoolean("dashes", options.normalization().dashes());
            }
            cicObject.putObject("normalization", norm);
        }

        if (options.chunking() != null) {
            cicObject.putBoolean("chunking", options.chunking());
        }
        if (options.chunkingStrategy() != null) {
            cicObject.putString("chunking_strategy", options.chunkingStrategy());
        }
        if (options.chunkSize() != null) {
            cicObject.putInt("chunk_size", options.chunkSize());
        }
        if (options.embedding() != null) {
            cicObject.putBoolean("embedding", options.embedding());
        }
        if (options.embeddingsModel() != null) {
            cicObject.putString("embeddings_model", options.embeddingsModel());
        }
        if (options.jsonSchema() != null) {
            if (options.jsonSchema() instanceof Boolean b) {
                cicObject.putBoolean("json_schema", b);
            } else {
                cicObject.putString("json_schema", options.jsonSchema().toString());
            }
        }
        if (options.pii() != null) {
            var pii = CICObject.create();
            if (options.pii().mode() != null) {
                pii.putString("mode", options.pii().mode());
            }
            if (options.pii().entityRedaction() != null) {
                pii.putBoolean("entity_redaction", options.pii().entityRedaction());
            }
            cicObject.putObject("pii", pii);
        }

        return cicObject;
    }
}
