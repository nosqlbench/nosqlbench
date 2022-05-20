package io.nosqlbench.driver.pulsar.ops;

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


import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.SchemaType;

public class PulsarReaderOp extends SyncPulsarOp {
    private final Reader<?> reader;
    private final Schema<?> pulsarSchema;
    private final boolean asyncPulsarOp;

    public PulsarReaderOp(Reader<?> reader, Schema<?> schema, boolean asyncPulsarOp) {
        this.reader = reader;
        this.pulsarSchema = schema;
        this.asyncPulsarOp = asyncPulsarOp;
    }

    public void syncRead() {
        try {
            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
            String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();

            // TODO: how many messages to read per NB cycle?
            Message<?> message;
            while (reader.hasMessageAvailable()) {
                message = reader.readNext();

                if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                    org.apache.avro.Schema avroSchema =
                        AvroUtil.GetSchema_ApacheAvro(avroDefStr);

                    org.apache.avro.generic.GenericRecord avroGenericRecord =
                        AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());

                    System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + avroGenericRecord.toString());
                } else {
                    System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + new String(message.getData()));
                }
            }
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void asyncRead() {
        //TODO: add support for async read
    }

    @Override
    public void run() {
        if (!asyncPulsarOp)
            syncRead();
        else
            asyncRead();
    }
}
