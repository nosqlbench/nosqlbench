package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.LongFunction;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.transaction.Transaction;

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
public class PulsarProducerMapper extends PulsarTransactOpMapper {

    private final static Logger logger = LogManager.getLogger(PulsarProducerMapper.class);

    private final LongFunction<Producer<?>> producerFunc;
    private final Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> seqErrSimuTypes;
    private final LongFunction<String> keyFunc;
    private final LongFunction<String> propFunc;
    private final LongFunction<String> payloadFunc;

    public PulsarProducerMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                PulsarActivity pulsarActivity,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Boolean> useTransactionFunc,
                                LongFunction<Boolean> seqTrackingFunc,
                                LongFunction<Supplier<Transaction>> transactionSupplierFunc,
                                LongFunction<Producer<?>> producerFunc,
                                Set<PulsarActivityUtil.SEQ_ERROR_SIMU_TYPE> seqErrSimuTypes,
                                LongFunction<String> keyFunc,
                                LongFunction<String> propFunc,
                                LongFunction<String> payloadFunc) {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc, useTransactionFunc, seqTrackingFunc, transactionSupplierFunc);

        this.producerFunc = producerFunc;
        this.seqErrSimuTypes = seqErrSimuTypes;
        this.keyFunc = keyFunc;
        this.propFunc = propFunc;
        this.payloadFunc = payloadFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        boolean asyncApi = asyncApiFunc.apply(value);
        boolean useTransaction = useTransactionFunc.apply(value);
        Supplier<Transaction> transactionSupplier = transactionSupplierFunc.apply(value);

        Producer<?> producer = producerFunc.apply(value);

        String msgKey = keyFunc.apply(value);
        String msgPayload = payloadFunc.apply(value);

        // Check if msgPropJonStr is valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message properties without throwing a runtime exception
        Map<String, String> msgProperties = new HashMap<>();
        String msgPropJsonStr = propFunc.apply(value);
        if (!StringUtils.isBlank(msgPropJsonStr)) {
            try {
                msgProperties = PulsarActivityUtil.convertJsonToMap(msgPropJsonStr);

            } catch (Exception e) {
                logger.error(
                    "Error parsing message property JSON string {}, ignore message properties!",
                    msgPropJsonStr);
            }
        }

        boolean sequenceTrackingEnabled = seqTrackingFunc.apply(value);
        if (sequenceTrackingEnabled) {
            long nextSequenceNumber = getMessageSequenceNumberSendingHandler(producer.getTopic())
                .getNextSequenceNumber(seqErrSimuTypes);
            msgProperties.put(PulsarActivityUtil.MSG_SEQUENCE_NUMBER, String.valueOf(nextSequenceNumber));
        }

        return new PulsarProducerOp(
            pulsarActivity,
            asyncApi,
            useTransaction,
            transactionSupplier,
            producer,
            clientSpace.getPulsarSchema(),
            msgKey,
            msgProperties,
            msgPayload);
    }

    private MessageSequenceNumberSendingHandler getMessageSequenceNumberSendingHandler(String topicName) {
        return MessageSequenceNumberSendingHandlersThreadLocal.get()
            .computeIfAbsent(topicName, k -> new MessageSequenceNumberSendingHandler());
    }

    private ThreadLocal<Map<String, MessageSequenceNumberSendingHandler>> MessageSequenceNumberSendingHandlersThreadLocal =
        ThreadLocal.withInitial(HashMap::new);

}
