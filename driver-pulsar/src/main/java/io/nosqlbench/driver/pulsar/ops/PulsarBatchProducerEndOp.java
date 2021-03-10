package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.pulsar.client.api.BatchMessageContainer;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.impl.BatchMessageContainerBase;
import org.apache.pulsar.client.impl.DefaultBatcherBuilder;
import org.apache.pulsar.common.util.FutureUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerEndOp implements PulsarOp {
    @Override
    public void run() {
        List<CompletableFuture<MessageId>> container = PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.get();
        Producer<?> producer = PulsarBatchProducerStartOp.threadLocalProducer.get();

        if ((container != null) && (!container.isEmpty())) {
            try {
                // producer.flushAsync().get();
                FutureUtil.waitForAll(container).get();
            } catch (Exception e) {
                throw new RuntimeException("Batch Producer:: failed to send (some of) the batched messages!");
            }

            container.clear();
            PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.set(null);
        } else {
            throw new BasicError("You tried to end an empty batch message container. This means you" +
                " did initiate the batch container properly, or there is an error in your" +
                " pulsar op sequencing and ratios.");
        }
    }
}
