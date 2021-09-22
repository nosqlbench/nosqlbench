package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public abstract class PulsarTransactOpMapper extends PulsarOpMapper {
    protected final LongFunction<Boolean> useTransactionFunc;
    protected final LongFunction<Boolean> seqTrackingFunc;
    protected final LongFunction<Supplier<Transaction>> transactionSupplierFunc;

    public PulsarTransactOpMapper(CommandTemplate cmdTpl,
                                  PulsarSpace clientSpace,
                                  PulsarActivity pulsarActivity,
                                  LongFunction<Boolean> asyncApiFunc,
                                  LongFunction<Boolean> useTransactionFunc,
                                  LongFunction<Boolean> seqTrackingFunc,
                                  LongFunction<Supplier<Transaction>> transactionSupplierFunc)
    {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc);
        this.useTransactionFunc = useTransactionFunc;
        this.seqTrackingFunc = seqTrackingFunc;
        this.transactionSupplierFunc = transactionSupplierFunc;
    }
}
