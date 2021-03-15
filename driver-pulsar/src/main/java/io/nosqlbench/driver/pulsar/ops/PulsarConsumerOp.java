package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.common.schema.SchemaType;

import java.util.concurrent.TimeUnit;

public class PulsarConsumerOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarConsumerOp.class);

    private final Consumer<?> consumer;
    private final Schema<?> pulsarSchema;
    private final boolean asyncPulsarOp;
    private final int timeoutSeconds;

    public PulsarConsumerOp(Consumer<?> consumer, Schema<?> schema, boolean asyncPulsarOp, int timeoutSeconds) {
        this.consumer = consumer;
        this.pulsarSchema = schema;
        this.asyncPulsarOp = asyncPulsarOp;
        this.timeoutSeconds = timeoutSeconds;
    }

    public void syncConsume() {
        try {
            Message<?> message = consumer.receive(timeoutSeconds, TimeUnit.SECONDS);
            if (message == null) {
                throw new RuntimeException("Did not receive a message within "+timeoutSeconds+" seconds");
            }

            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
            if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                if (logger.isDebugEnabled()) {
                    String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                    org.apache.avro.generic.GenericRecord avroGenericRecord =
                        AvroUtil.GetGenericRecord_ApacheAvro(avroDefStr, message.getData());

                    logger.debug("msg-key={}  msg-payload={}", message.getKey(), avroGenericRecord.toString());
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("msg-key={}  msg-payload={}", message.getKey(), new String(message.getData()));
                }
            }

            consumer.acknowledge(message.getMessageId());
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void asyncConsume() {
        //TODO: add support for async consume
    }

    @Override
    public void run() {
        if (!asyncPulsarOp)
            syncConsume();
        else
            asyncConsume();
    }
}
