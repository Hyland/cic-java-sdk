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
public class CICObjectDeserializer extends JsonDeserializer<CICObject> {

    @Override
    public CICObject deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return deserializeObject(parser);
    }

    protected CICObject deserializeObject(JsonParser parser) throws IOException {
        if (parser.currentToken() != JsonToken.START_OBJECT) {
            throw new IOException("not at the beginning of an object");
        }
        var cicObject = CICObject.create();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() != JsonToken.FIELD_NAME) {
                throw new IOException("not at the beginning of an object");

            }
            String fieldName = parser.getText();
            switch (parser.nextToken()) {
                case VALUE_STRING -> cicObject.putString(fieldName, parser.getText());
                case VALUE_FALSE -> cicObject.putBoolean(fieldName, false);
                case VALUE_TRUE -> cicObject.putBoolean(fieldName, true);
                case VALUE_NUMBER_INT -> cicObject.putLong(fieldName, parser.getLongValue());
                case VALUE_NUMBER_FLOAT -> {
                    // TODO
                }
                case VALUE_NULL -> {
                    // TODO
                }
                case START_OBJECT -> cicObject.putObject(fieldName, deserializeObject(parser));
                case START_ARRAY -> cicObject.putArray(fieldName, deserializeArray(parser));
            }
        }
        return cicObject;
    }

    protected CICArray deserializeArray(JsonParser parser) throws IOException {
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
                case VALUE_NUMBER_FLOAT -> {
                    // TODO
                }
                case VALUE_NULL -> {
                    // TODO
                }
                case START_OBJECT -> cicArray.addObject(deserializeObject(parser));
                case START_ARRAY -> cicArray.addArray(deserializeArray(parser));
            }
        }
        return cicArray;
    }
}
