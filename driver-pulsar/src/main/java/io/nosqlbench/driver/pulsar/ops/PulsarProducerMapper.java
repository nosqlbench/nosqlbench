package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;
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
public class PulsarProducerMapper extends PulsarOpMapper {
    private final LongFunction<Producer<?>> producerFunc;
    private final LongFunction<String> keyFunc;
    private final LongFunction<String> payloadFunc;
    private final PulsarActivity pulsarActivity;
    private final LongFunction<Boolean> useTransactionFunc;
    private final LongFunction<Supplier<Transaction>> transactionSupplierFunc;

    public PulsarProducerMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Producer<?>> producerFunc,
                                LongFunction<String> keyFunc,
                                LongFunction<String> payloadFunc,
                                LongFunction<Boolean> useTransactionFunc,
                                LongFunction<Supplier<Transaction>> transactionSupplierFunc,
                                PulsarActivity pulsarActivity) {
        super(cmdTpl, clientSpace, asyncApiFunc);
        this.producerFunc = producerFunc;
        this.keyFunc = keyFunc;
        this.payloadFunc = payloadFunc;
        this.pulsarActivity = pulsarActivity;
        this.useTransactionFunc = useTransactionFunc;
        this.transactionSupplierFunc = transactionSupplierFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Producer<?> producer = producerFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);
        String msgKey = keyFunc.apply(value);
        String msgPayload = payloadFunc.apply(value);
        boolean useTransaction = useTransactionFunc.apply(value);
        Supplier<Transaction> transactionSupplier = transactionSupplierFunc.apply(value);
        return new PulsarProducerOp(
            producer,
            clientSpace.getPulsarSchema(),
            asyncApi,
            useTransaction,
            transactionSupplier,
            msgKey,
            msgPayload,
            pulsarActivity
            );
    }
}
