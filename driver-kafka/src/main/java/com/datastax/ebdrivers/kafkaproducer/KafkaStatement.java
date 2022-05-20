package com.datastax.ebdrivers.kafkaproducer;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.avro.AvroSchemaUtils;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.ParsedTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaStatement {
    private final static Logger logger = LogManager.getLogger(KafkaStatement.class);

    private Producer<Object,Object> producer = null;
    private final StringBindings bindings;
    private final String topic;
    private final String keySerializerClass;
    private final String valueSerializerClass;
    private AvroSchema keySerializerSchema = null;
    private AvroSchema valueSerializerSchema = null;
    private final String key;

    public KafkaStatement(OpTemplate stmtDef, String servers, String clientId, String schemaRegistryUrl) {
        ParsedTemplate paramTemplate = new ParsedTemplate(stmtDef.getStmt().orElseThrow(), stmtDef.getBindings());
        BindingsTemplate paramBindings = new BindingsTemplate(paramTemplate.getBindPoints());
        StringBindingsTemplate template = new StringBindingsTemplate(stmtDef.getStmt().orElseThrow(), paramBindings);

        this.bindings = template.resolve();

        // Process key serializer class and schema, if any
        this.keySerializerClass =
            stmtDef.getOptionalStringParam("key_serializer_class")
                .orElse(StringSerializer.class.getName());

        Optional<String> keySerializerSchemaFile =
            stmtDef.getOptionalStringParam("key_serializer_schema_file");

        if (keySerializerClass.equals("io.confluent.kafka.serializers.KafkaAvroSerializer")
            && keySerializerSchemaFile.isEmpty() ) {
            throw new RuntimeException("KafkaAvroSerializer requires key_serializer_schema_file");
        }

        if (keySerializerSchemaFile.isPresent()) {
            Path schemaFilePath = Path.of(keySerializerSchemaFile.get());
            try {
                this.keySerializerSchema = new AvroSchema(Files.readString(schemaFilePath));
            } catch (IOException e) {
                throw new RuntimeException("Error reading key schema file: " + keySerializerSchemaFile, e);
            }
        }

        // Process value serializer class and schema, if any
        this.valueSerializerClass =
            stmtDef.getOptionalStringParam("value_serializer_class")
                .orElse(StringSerializer.class.getName());

        Optional<String> valueSerializerSchemaFile =
            stmtDef.getOptionalStringParam("value_serializer_schema_file");

        if (valueSerializerClass.equals("io.confluent.kafka.serializers.KafkaAvroSerializer")
            && valueSerializerSchemaFile.isEmpty() ) {
            throw new RuntimeException("KafkaAvroSerializer requires value_serializer_schema_file");
        }

        if (valueSerializerSchemaFile.isPresent()) {
            Path schemaFilePath = Path.of(valueSerializerSchemaFile.get());
            try {
                this.valueSerializerSchema = new AvroSchema(Files.readString(schemaFilePath));
            } catch (IOException e) {
                throw new RuntimeException("Error reading value schema file: " + valueSerializerSchemaFile, e);
            }
        }

        this.topic = stmtDef.getParamOrDefault("topic","default-topic");
        this.key = stmtDef.getOptionalStringParam("key").orElse("key");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  servers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializerClass);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializerClass);
        props.put("schema.registry.url", schemaRegistryUrl);

        try {
            producer = new KafkaProducer<>(props);
        } catch (Exception e) {
            logger.error("Error constructing kafka producer", e);
        }
    }

    private Object bindKey(long cycle) {
        Object statement = key;
        if (keySerializerClass != null &&
            keySerializerClass.equals("io.confluent.kafka.serializers.KafkaAvroSerializer")) {
            try {
                statement = AvroSchemaUtils.toObject((String)statement, keySerializerSchema);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return statement;
    }

    private Object bindValue(long cycle) {
        Object statement = bindings.bind(cycle);
        if (valueSerializerClass != null &&
            valueSerializerClass.equals("io.confluent.kafka.serializers.KafkaAvroSerializer")) {
            try {
                statement = AvroSchemaUtils.toObject((String)statement, valueSerializerSchema);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return statement;
    }

    public void write(long cycle) {
        Object key = bindKey(cycle);
        Object value = bindValue(cycle);
        try {
            ProducerRecord<Object, Object> record = new ProducerRecord<>(topic, key, value);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata result = send.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
