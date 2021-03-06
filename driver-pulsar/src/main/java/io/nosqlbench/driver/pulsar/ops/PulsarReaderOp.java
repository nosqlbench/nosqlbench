package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.SchemaType;

public class PulsarReaderOp implements PulsarOp {
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
                    org.apache.avro.generic.GenericRecord avroGenericRecord =
                        AvroUtil.GetGenericRecord_ApacheAvro(avroDefStr, message.getData());
                    System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + avroGenericRecord.toString());
                }
                else {
                    System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + new String(message.getData()));
                }
            }
        }
        catch (PulsarClientException e) {
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
