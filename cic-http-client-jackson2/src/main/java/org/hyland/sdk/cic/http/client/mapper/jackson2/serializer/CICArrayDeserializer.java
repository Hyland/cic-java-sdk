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
 *     Damian Ujma <damian.ujma@hyland.com>
 */
package org.hyland.sdk.cic.http.client.mapper.jackson2.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import org.hyland.sdk.cic.http.client.mapper.object.CICArray;
import org.hyland.sdk.cic.http.client.mapper.object.CICObject;

/**
 * @since 1.0.0
 */
public class CICArrayDeserializer extends JsonDeserializer<CICArray> {

    @Override
    public CICArray deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) {
            throw new IOException("not at the beginning of an array");
        }
        var cicArray = CICArray.create();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            switch (parser.currentToken()) {
                case VALUE_STRING -> cicArray.addString(parser.getText());
                case VALUE_FALSE -> cicArray.addBoolean(false);
                case VALUE_TRUE -> cicArray.addBoolean(true);
                case VALUE_NUMBER_INT -> cicArray.addLong(parser.getLongValue());
                case VALUE_NUMBER_FLOAT -> cicArray.addDouble(parser.getDoubleValue());
                case VALUE_NULL -> {
                    // Skip null values - they won't be included in the array
                }
                case START_OBJECT -> cicArray.addObject(context.readValue(parser, CICObject.class));
                case START_ARRAY -> cicArray.addArray(context.readValue(parser, CICArray.class));
            }
        }
        return cicArray;
    }
}
