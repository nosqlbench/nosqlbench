package io.nosqlbench.driver.pulsar.util;

import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.io.BinaryDecoder;
import org.apache.pulsar.client.api.schema.Field;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.schema.GenericRecordBuilder;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class AvroUtil {
    public static org.apache.avro.Schema GetSchema_ApacheAvro(String avroSchemDef) {
        return new org.apache.avro.Schema.Parser().parse(avroSchemDef);
    }
    public static org.apache.avro.generic.GenericRecord GetGenericRecord_ApacheAvro(String avroSchemDef, String jsonData)  {
        org.apache.avro.generic.GenericRecord record = null;

        try {
            org.apache.avro.Schema schema = GetSchema_ApacheAvro(avroSchemDef);

            org.apache.avro.generic.GenericDatumReader<org.apache.avro.generic.GenericData.Record> reader;
            reader = new org.apache.avro.generic.GenericDatumReader<>(schema);

            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, jsonData);

            record = reader.read(null, decoder);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return record;
    }
    public static org.apache.avro.generic.GenericRecord GetGenericRecord_ApacheAvro(String avroSchemDef, byte[] bytesData)  {
        org.apache.avro.generic.GenericRecord record = null;

        try {
            org.apache.avro.Schema schema = GetSchema_ApacheAvro(avroSchemDef);

            org.apache.avro.generic.GenericDatumReader<org.apache.avro.generic.GenericData.Record> reader;
            reader = new org.apache.avro.generic.GenericDatumReader<>(schema);

            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(bytesData, null);

            record = reader.read(null, decoder);
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return record;
    }

    public static GenericAvroSchema GetSchema_PulsarAvro(String schemaName, String avroSchemDef) {
        SchemaInfo schemaInfo = SchemaInfo.builder()
            .schema(avroSchemDef.getBytes(StandardCharsets.UTF_8))
            .type(SchemaType.AVRO)
            .properties(new HashMap<>())
            .name(schemaName)
            .build();
        return new GenericAvroSchema(schemaInfo);
    }
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
    public static GenericRecord GetGenericRecord_PulsarAvro(String schemaName, String avroSchemDef, String jsonData) {
        GenericAvroSchema genericAvroSchema = GetSchema_PulsarAvro(schemaName, avroSchemDef);
        org.apache.avro.generic.GenericRecord apacheAvroRecord = GetGenericRecord_ApacheAvro(avroSchemDef, jsonData);

        return GetGenericRecord_PulsarAvro(genericAvroSchema, apacheAvroRecord);
    }
}
