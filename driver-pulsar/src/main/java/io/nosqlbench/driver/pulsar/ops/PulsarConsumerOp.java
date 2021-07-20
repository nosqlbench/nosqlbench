package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.common.schema.SchemaType;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class PulsarConsumerOp extends SyncPulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarConsumerOp.class);

    private final Consumer<?> consumer;
    private final Schema<?> pulsarSchema;
    private final boolean asyncPulsarOp;
    private final int timeoutSeconds;
    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;
    private final boolean useTransaction;
    private final Supplier<Transaction> transactionSupplier;
    private final Timer transactionCommitTimer;

    public PulsarConsumerOp(Consumer<?> consumer, Schema<?> schema, boolean asyncPulsarOp, int timeoutSeconds,
                            Counter bytesCounter,
                            Histogram messagesizeHistogram,
                            boolean useTransaction,
                            Supplier<Transaction> transactionSupplier,
                            Timer transactionCommitTimer) {
        this.consumer = consumer;
        this.pulsarSchema = schema;
        this.asyncPulsarOp = asyncPulsarOp;
        this.timeoutSeconds = timeoutSeconds;
        this.bytesCounter = bytesCounter;
        this.messagesizeHistogram = messagesizeHistogram;
        this.useTransaction = useTransaction;
        this.transactionSupplier = transactionSupplier;
        this.transactionCommitTimer = transactionCommitTimer;
    }

    public void syncConsume() {
        try {
            Message<?> message;
            if (timeoutSeconds <= 0) {
                // wait forever
                message = consumer.receive();
            } else {
                // we cannot use Consumer#receive(timeout, timeunit) due to
                // https://github.com/apache/pulsar/issues/9921
                message = consumer
                    .receiveAsync()
                    .get(timeoutSeconds, TimeUnit.SECONDS);
            }

            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
            if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                if (logger.isDebugEnabled()) {
                    String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();

                    org.apache.avro.Schema avroSchema =
                        AvroUtil.GetSchema_ApacheAvro(avroDefStr);

                    org.apache.avro.generic.GenericRecord avroGenericRecord =
                        AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());

                    logger.debug("msg-key={}  msg-payload={}", message.getKey(), avroGenericRecord.toString());
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("msg-key={}  msg-payload={}", message.getKey(), new String(message.getData()));
                }
            }
            int messagesize = message.getData().length;
            bytesCounter.inc(messagesize);
            messagesizeHistogram.update(messagesize);


            if (useTransaction) {
                Transaction transaction = transactionSupplier.get();
                consumer.acknowledgeAsync(message.getMessageId(), transaction).get();

                // little problem: here we are counting the "commit" time
                // inside the overall time spent for the execution of the consume operation
                // we should refactor this operation as for PulsarProducerOp, and use the passed callback
                // to track with precision the time spent for the operation and for the commit
                try (Timer.Context ctx = transactionCommitTimer.time()) {
                    transaction.commit().get();
                }
            } else{
                consumer.acknowledge(message.getMessageId());
            }
        } catch (Exception e) {
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
