package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.commons.compress.utils.Lists;
import org.apache.pulsar.client.api.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerStartOp extends SimplePulsarOp {

    // TODO: ensure sane container lifecycle management
    public final static ThreadLocal<List<CompletableFuture<MessageId>>> threadLocalBatchMsgContainer = new ThreadLocal<>();
    public final static ThreadLocal<Producer<?>> threadLocalProducer = new ThreadLocal<>();

    public PulsarBatchProducerStartOp(Producer<?> batchProducer) {
        threadLocalProducer.set(batchProducer);
    }

    @Override
    public void run() {
        List<CompletableFuture<MessageId>> container = threadLocalBatchMsgContainer.get();

        if (container == null) {
            container = Lists.newArrayList();
            threadLocalBatchMsgContainer.set(container);
        } else {
            throw new BasicError("You tried to create a batch message container where one was already" +
                " defined. This means you did not flush and unset the last container, or there is an error in your" +
                " pulsar op sequencing and ratios.");
        }
    }
}
