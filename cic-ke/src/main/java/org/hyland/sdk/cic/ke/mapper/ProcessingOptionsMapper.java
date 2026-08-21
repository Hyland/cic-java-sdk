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
import org.hyland.sdk.cic.http.client.mapper.object.CICPrimitive;
import org.hyland.sdk.cic.ke.object.NormalizationOptions;
import org.hyland.sdk.cic.ke.object.PiiOptions;
import org.hyland.sdk.cic.ke.object.ProcessingOptions;

/**
 * Bidirectional mapper for {@link ProcessingOptions}.
 * <p>
 * Reads both the nested format returned by config endpoints and the flat legacy format used on presign submit. Writes
 * using the nested format when strategy/size/model/precision details are present.
 *
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

        readChunking(cicObject, builder);
        readEmbedding(cicObject, builder);

        var jsonSchemaNode = cicObject.getProperties().get("json_schema");
        if (jsonSchemaNode instanceof CICPrimitive.CICBoolean b) {
            builder.jsonSchema(b.value());
        } else if (jsonSchemaNode instanceof CICPrimitive.CICString s) {
            builder.jsonSchema(s.value());
        }

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

    private void readChunking(CICObject cicObject, ProcessingOptions.Builder builder) {
        var chunkingNode = cicObject.getProperties().get("chunking");
        if (chunkingNode instanceof CICPrimitive.CICBoolean b) {
            builder.chunking(b.value());
            cicObject.getOptionalString("chunking_strategy").ifPresent(builder::chunkingStrategy);
            var chunkSize = cicObject.getIntegerOrNull("chunk_size");
            if (chunkSize != null) {
                builder.chunkSize(chunkSize);
            }
        } else if (chunkingNode instanceof CICObject chunkingObj) {
            builder.chunking(true);
            chunkingObj.getOptionalString("strategy").ifPresent(builder::chunkingStrategy);
            var cs = chunkingObj.getIntegerOrNull("chunk_size");
            if (cs != null) {
                builder.chunkSize(cs);
            }
        }
    }

    private void readEmbedding(CICObject cicObject, ProcessingOptions.Builder builder) {
        var embeddingNode = cicObject.getProperties().get("embedding");
        if (embeddingNode instanceof CICPrimitive.CICBoolean b) {
            builder.embedding(b.value());
            cicObject.getOptionalString("embeddings_model").ifPresent(builder::embeddingsModel);
        } else if (embeddingNode instanceof CICObject embeddingObj) {
            builder.embedding(true);
            embeddingObj.getOptionalString("model").ifPresent(builder::embeddingsModel);
            embeddingObj.getOptionalString("precision").ifPresent(builder::embeddingPrecision);
        }
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

        writeChunking(options, cicObject);
        writeEmbedding(options, cicObject);

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

    private void writeChunking(ProcessingOptions options, CICObject cicObject) {
        boolean hasDetails = options.chunkingStrategy() != null || options.chunkSize() != null;
        if (hasDetails) {
            var chunkObj = CICObject.create();
            if (options.chunkingStrategy() != null) {
                chunkObj.putString("strategy", options.chunkingStrategy());
            }
            if (options.chunkSize() != null) {
                chunkObj.putInt("chunk_size", options.chunkSize());
            }
            cicObject.putObject("chunking", chunkObj);
        } else if (options.chunking() != null) {
            cicObject.putBoolean("chunking", options.chunking());
        }
    }

    private void writeEmbedding(ProcessingOptions options, CICObject cicObject) {
        boolean hasDetails = options.embeddingsModel() != null || options.embeddingPrecision() != null;
        if (hasDetails) {
            var embObj = CICObject.create();
            if (options.embeddingsModel() != null) {
                embObj.putString("model", options.embeddingsModel());
            }
            if (options.embeddingPrecision() != null) {
                embObj.putString("precision", options.embeddingPrecision());
            }
            cicObject.putObject("embedding", embObj);
        } else if (options.embedding() != null) {
            cicObject.putBoolean("embedding", options.embedding());
        }
    }
}
