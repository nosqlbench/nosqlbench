package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
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
public class PulsarConsumerMapper extends PulsarTransactOpMapper {

    private final static Logger logger = LogManager.getLogger(PulsarProducerMapper.class);

    private final LongFunction<Consumer<?>> consumerFunc;
    private final boolean e2eMsProc;

    public PulsarConsumerMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                PulsarActivity pulsarActivity,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Boolean> useTransactionFunc,
                                LongFunction<Boolean> seqTrackingFunc,
                                LongFunction<Supplier<Transaction>> transactionSupplierFunc,
                                LongFunction<Consumer<?>> consumerFunc,
                                boolean e2eMsgProc) {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc, useTransactionFunc, seqTrackingFunc, transactionSupplierFunc);
        this.consumerFunc = consumerFunc;
        this.e2eMsProc = e2eMsgProc;
    }

    @Override
    public PulsarOp apply(long value) {
        boolean seqTracking = seqTrackingFunc.apply(value);
        Consumer<?> consumer = consumerFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);
        boolean useTransaction = useTransactionFunc.apply(value);
        Supplier<Transaction> transactionSupplier = transactionSupplierFunc.apply(value);

        return new PulsarConsumerOp(
            pulsarActivity,
            asyncApi,
            useTransaction,
            seqTracking,
            transactionSupplier,
            consumer,
            clientSpace.getPulsarSchema(),
            clientSpace.getPulsarClientConf().getConsumerTimeoutSeconds(),
            e2eMsProc,
            this::getReceivedMessageSequenceTracker);
    }


    private ReceivedMessageSequenceTracker getReceivedMessageSequenceTracker(String topicName) {
        return receivedMessageSequenceTrackersForTopicThreadLocal.get()
            .computeIfAbsent(topicName, k -> createReceivedMessageSequenceTracker());
    }

    private ReceivedMessageSequenceTracker createReceivedMessageSequenceTracker() {
        return new ReceivedMessageSequenceTracker(pulsarActivity.getMsgErrOutOfSeqCounter(),
            pulsarActivity.getMsgErrDuplicateCounter(),
            pulsarActivity.getMsgErrLossCounter());
    }

    private final ThreadLocal<Map<String, ReceivedMessageSequenceTracker>> receivedMessageSequenceTrackersForTopicThreadLocal =
        ThreadLocal.withInitial(HashMap::new);

}
