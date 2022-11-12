/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.pulsar.util;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.pulsar.client.api.schema.Field;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.schema.GenericRecordBuilder;
import org.apache.pulsar.client.impl.schema.SchemaInfoImpl;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class AvroUtil {
    ////////////////////////
    // Get an OSS Apache Avro schema from a string definition
    public static org.apache.avro.Schema GetSchema_ApacheAvro(String avroSchemDef) {
        return new org.apache.avro.Schema.Parser().parse(avroSchemDef);
    }

    // Get an OSS Apache Avro schema record from a JSON string that matches a specific OSS Apache Avro schema
    public static org.apache.avro.generic.GenericRecord GetGenericRecord_ApacheAvro(org.apache.avro.Schema schema, String jsonData)  {
        org.apache.avro.generic.GenericRecord record = null;

        try {
            org.apache.avro.generic.GenericDatumReader<org.apache.avro.generic.GenericData.Record> reader;
            reader = new org.apache.avro.generic.GenericDatumReader<>(schema);

            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonData);

            record = reader.read(null, decoder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return record;
    }

    // Get an OSS Apache Avro schema record from a byte array that matches a specific OSS Apache Avro schema
    public static org.apache.avro.generic.GenericRecord GetGenericRecord_ApacheAvro(org.apache.avro.Schema schema, byte[] bytesData) {
        org.apache.avro.generic.GenericRecord record = null;

        try {
            org.apache.avro.generic.GenericDatumReader<org.apache.avro.generic.GenericData.Record> reader;
            reader = new org.apache.avro.generic.GenericDatumReader<>(schema);

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytesData, null);

            record = reader.read(null, decoder);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return record;
    }


    ////////////////////////
    // Get a Pulsar Avro schema from a string definition
    public static GenericAvroSchema GetSchema_PulsarAvro(String schemaName, String avroSchemDef) {
        SchemaInfo schemaInfo = SchemaInfoImpl.builder()
            .schema(avroSchemDef.getBytes(StandardCharsets.UTF_8))
            .type(SchemaType.AVRO)
            .properties(new HashMap<>())
            .name(schemaName)
            .build();
        return new GenericAvroSchema(schemaInfo);
    }

    // Get a Pulsar Avro record from an OSS Avro schema record, matching a specific Pulsar Avro schema
    public static GenericRecord GetGenericRecord_PulsarAvro(
        GenericAvroSchema pulsarGenericAvroSchema,
        org.apache.avro.generic.GenericRecord apacheAvroGenericRecord)
    {
        GenericRecordBuilder recordBuilder = pulsarGenericAvroSchema.newRecordBuilder();

        List<Field> fieldList = pulsarGenericAvroSchema.getFields();
        for (Field field : fieldList) {
            String fieldName = field.getName();
            recordBuilder.set(fieldName, apacheAvroGenericRecord.get(fieldName));
        }

        return recordBuilder.build();
    }

    // Get a Pulsar Avro record (GenericRecord) from a JSON string that matches a specific Pulsar Avro schema
    public static GenericRecord GetGenericRecord_PulsarAvro(GenericAvroSchema genericAvroSchema, String avroSchemDefStr, String jsonData) {
        org.apache.avro.Schema avroSchema = GetSchema_ApacheAvro(avroSchemDefStr);
        return GetGenericRecord_PulsarAvro(genericAvroSchema, avroSchema, jsonData);
    }

    public static GenericRecord GetGenericRecord_PulsarAvro(GenericAvroSchema genericAvroSchema, org.apache.avro.Schema avroSchema, String jsonData) {
        org.apache.avro.generic.GenericRecord apacheAvroRecord = GetGenericRecord_ApacheAvro(avroSchema, jsonData);
        return GetGenericRecord_PulsarAvro(genericAvroSchema, apacheAvroRecord);
    }
    public static GenericRecord GetGenericRecord_PulsarAvro(String schemaName, String avroSchemDefStr, String jsonData) {
        GenericAvroSchema genericAvroSchema = GetSchema_PulsarAvro(schemaName, avroSchemDefStr);
        org.apache.avro.Schema avroSchema = GetSchema_ApacheAvro(avroSchemDefStr);
        org.apache.avro.generic.GenericRecord apacheAvroRecord = GetGenericRecord_ApacheAvro(avroSchema, jsonData);

        return GetGenericRecord_PulsarAvro(genericAvroSchema, apacheAvroRecord);
    }
}
