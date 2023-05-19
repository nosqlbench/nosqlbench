/*
 * Copyright (c) 2022-2023 nosqlbench
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
 */
package io.nosqlbench.adapter.venice.util;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AvroUtils {
    private static final Logger logger = LogManager.getLogger("AvroUtils");

    public static org.apache.avro.Schema parseAvroSchema(String avroSchemDef) {
        return new org.apache.avro.Schema.Parser().parse(avroSchemDef);
    }

    public static Object encodeToAvro(org.apache.avro.Schema schema, String jsonData)  {
        if (schema.getType() == Schema.Type.STRING) {
            return jsonData;
        } else if (schema.getType() == Schema.Type.RECORD) {
            org.apache.avro.generic.GenericRecord record = null;

            try {
                org.apache.avro.generic.GenericDatumReader<org.apache.avro.generic.GenericData.Record> reader;
                reader = new org.apache.avro.generic.GenericDatumReader<>(schema);

                JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonData);

                record = reader.read(null, decoder);
            } catch (IOException ioe) {
                logger.info("Cannot convert JSON {} to AVRO: ", jsonData, ioe);
                throw new RuntimeException(ioe);
            }

            return record;
        } else {
            throw new RuntimeException("Unsupported schema + " + schema.getType()+ ", only string and record");
        }
    }

}
