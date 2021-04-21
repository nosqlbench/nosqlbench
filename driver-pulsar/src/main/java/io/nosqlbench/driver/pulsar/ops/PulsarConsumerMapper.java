package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class PulsarConsumerMapper extends PulsarOpMapper {
    private final LongFunction<Consumer<?>> consumerFunc;
    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;
    private final LongFunction<Boolean> useTransactionFunc;
    private final LongFunction<Supplier<Transaction>> transactionSupplierFunc;
    private final Timer transactionCommitTimer;

    public PulsarConsumerMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Consumer<?>> consumerFunc,
                                Counter bytesCounter,
                                Histogram messagesizeHistogram,
                                Timer transactionCommitTimer,
                                LongFunction<Boolean> useTransactionFunc,
                                LongFunction<Supplier<Transaction>> transactionSupplierFunc) {
        super(cmdTpl, clientSpace, asyncApiFunc);
        this.consumerFunc = consumerFunc;
        this.bytesCounter = bytesCounter;
        this.messagesizeHistogram = messagesizeHistogram;
        this.transactionCommitTimer = transactionCommitTimer;
        this.useTransactionFunc = useTransactionFunc;
        this.transactionSupplierFunc = transactionSupplierFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Consumer<?> consumer = consumerFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);
        boolean useTransaction = useTransactionFunc.apply(value);
        Supplier<Transaction> transactionSupplier = transactionSupplierFunc.apply(value);

        return new PulsarConsumerOp(
            consumer,
            clientSpace.getPulsarSchema(),
            asyncApi,
            clientSpace.getPulsarClientConf().getConsumerTimeoutSeconds(),
            bytesCounter,
            messagesizeHistogram,
            useTransaction,
            transactionSupplier,
            transactionCommitTimer
        );
    }
}
