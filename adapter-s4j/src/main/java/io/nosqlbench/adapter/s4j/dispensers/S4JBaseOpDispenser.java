package io.nosqlbench.adapter.s4j.dispensers;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import com.datastax.oss.pulsar.jms.PulsarJMSContext;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.ops.S4JOp;
import io.nosqlbench.adapter.s4j.util.*;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract  class S4JBaseOpDispenser extends BaseOpDispenser<S4JOp, S4JSpace> {

    private final static Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final S4JSpace s4jSpace;
    protected final S4JAdapterMetrics s4jAdapterMetrics;

    private final ConcurrentHashMap<String, JMSContext> connLvlJmsContexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, S4JJMSContextWrapper> sessionLvlJmsContexts = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, JMSProducer> jmsProducers = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, JMSConsumer> jmsConsumers = new ConcurrentHashMap<>();

    // Doc-level parameter: temporary_dest (default: false)
    protected final boolean temporaryDest;
    // Doc-level parameter: dest_type (default: Topic)
    protected final String destType;
    // Doc-level parameter: async_api (default: true)
    protected final boolean asyncAPI;
    // Doc-level parameter: txn_batch_num (default: 0)
    // - value <=0 : no transaction
    protected final int txnBatchNum;

    protected final LongFunction<String> destNameStrFunc;

    protected final int totalThreadNum;
    protected final long totalCycleNum;

    public S4JBaseOpDispenser(DriverAdapter adapter,
                              ParsedOp op,
                              LongFunction<String> destNameStrFunc,
                              S4JSpace s4jSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.s4jSpace = s4jSpace;
        this.connLvlJmsContexts.putAll(s4jSpace.getConnLvlJmsContexts());
        this.sessionLvlJmsContexts.putAll(s4jSpace.getSessionLvlJmsContexts());

        String defaultMetricsPrefix = getDefaultMetricsPrefix(this.parsedOp);
        this.s4jAdapterMetrics = new S4JAdapterMetrics(defaultMetricsPrefix);
        s4jAdapterMetrics.initS4JAdapterInstrumentation();

        this.destNameStrFunc = destNameStrFunc;
        this.temporaryDest =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.TEMP_DEST.label, Boolean.FALSE);
        this.destType =
            parsedOp.getStaticConfig(S4JAdapterUtil.DOC_LEVEL_PARAMS.DEST_TYPE.label, String.class);
        this.asyncAPI =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, Boolean.TRUE);
        this.txnBatchNum =
            parsedOp.getStaticConfigOr(S4JAdapterUtil.DOC_LEVEL_PARAMS.TXN_BATCH_NUM.label, Integer.valueOf(0));

        this.totalThreadNum = NumberUtils.toInt(parsedOp.getStaticValue("threads"));
        this.totalCycleNum = NumberUtils.toLong(parsedOp.getStaticValue("cycles"));
        s4jSpace.setTotalCycleNum(totalCycleNum);
    }

    public S4JSpace getS4jSpace() { return s4jSpace; }
    public S4JAdapterMetrics getS4jAdapterMetrics() { return s4jAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(String paramName, boolean defaultValue) {
        LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = (l) -> parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(String paramName) {
        LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = (l) -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<String > set = new HashSet<>();

                if (StringUtils.contains(value,',')) {
                    set = Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(Predicate.not(String::isEmpty))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                }

                return set;
            }).orElse(Collections.emptySet());
        logger.info("{}: {}", paramName, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<Integer> lookupStaticIntOpValueFunc(String paramName, int defaultValue) {
        LongFunction<Integer> integerLongFunction;
        integerLongFunction = (l) -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> NumberUtils.toInt(value))
            .map(value -> {
                if (value < 0) return 0;
                else return value;
            }).orElse(defaultValue);
        logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse((l) -> defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName) {
        return lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    /**
     * If the JMS destination that corresponds to a topic exists, reuse it; Otherwise, create it
     */
    public Destination getOrCreateJmsDestination(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        boolean tempDest,
        String destType,
        String destName) throws JMSRuntimeException
    {
        String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        // Regular, non-temporary destination
        if (!tempDest) {
            String destinationCacheKey = S4JAdapterUtil.buildCacheKey(jmsContextIdStr, destType, destName);
            Destination destination = jmsDestinations.get(destinationCacheKey);

            if (destination == null) {
                if (StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.QUEUE.label)) {
                    destination = jmsContext.createQueue(destName);
                } else  {
                    destination = jmsContext.createTopic(destName);
                }

                jmsDestinations.put(destinationCacheKey, destination);
            }

            return destination;
        }
        // Temporary destination
        else {
            if (StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.QUEUE.label)) {
                return jmsContext.createTemporaryQueue();
            } else  {
                return jmsContext.createTemporaryTopic();
            }
        }
    }

    // Get simplified NB thread name
    private String getSimplifiedNBThreadName(String fullThreadName) {
        assert (StringUtils.isNotBlank(fullThreadName));

        if (StringUtils.contains(fullThreadName, '/'))
            return StringUtils.substringAfterLast(fullThreadName, "/");
        else
            return fullThreadName;
    }


    /**
     * If the JMS producer that corresponds to a destination exists, reuse it; Otherwise, create it
     */
    public JMSProducer getOrCreateJmsProducer(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        boolean asyncApi) throws JMSException
    {
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        String producerCacheKey = S4JAdapterUtil.buildCacheKey(
            getSimplifiedNBThreadName(Thread.currentThread().getName()), "producer");
        JMSProducer jmsProducer = jmsProducers.get(producerCacheKey);

        if (jmsProducer == null) {
            jmsProducer = jmsContext.createProducer();

            if (asyncApi) {
                jmsProducer.setAsync(new S4JCompletionListener(s4jSpace, this));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Producer created: {} -- {} -- {}",
                    producerCacheKey, jmsProducer, s4JJMSContextWrapper);
            }

            jmsProducers.put(producerCacheKey, jmsProducer);
        }

        return jmsProducer;
    }

    /**
     * If the JMS consumer that corresponds to a destination(, subscription, message selector) exists, reuse it; Otherwise, create it
     */
    public JMSConsumer getOrCreateJmsConsumer(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        Destination destination,
        String destType,
        String subName,
        String msgSelector,
        float msgAckRatio,
        boolean nonLocal,
        boolean durable,
        boolean shared,
        boolean asyncApi,
        int slowAckInSec) throws JMSException
    {
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean isTopic = StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.TOPIC.label);
        String consumerCacheKey = S4JAdapterUtil.buildCacheKey(
            getSimplifiedNBThreadName(Thread.currentThread().getName()), "consumer");

        JMSConsumer jmsConsumer = jmsConsumers.get(consumerCacheKey);
        if (jmsConsumer == null) {
            if (isTopic) {
                if (!durable && !shared)
                    jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);
                else {
                    if (StringUtils.isBlank(subName)) {
                        throw new RuntimeException("Subscription name is required for receiving messages from a durable or shared topic!");
                    }

                    if (durable && !shared)
                        jmsConsumer = jmsContext.createDurableConsumer(
                            (Topic) destination, subName, msgSelector, nonLocal);
                    else if (!durable)
                        jmsConsumer = jmsContext.createSharedConsumer((Topic) destination, subName, msgSelector);
                    else
                        jmsConsumer = jmsContext.createSharedDurableConsumer((Topic) destination, subName, msgSelector);
                }
            }
            else {
                jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);
            }

            if (asyncApi) {
                jmsConsumer.setMessageListener(
                    new S4JMessageListener(jmsContext, s4jSpace, this, msgAckRatio, slowAckInSec));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Consumer created: {} -- {} -- {}",
                    consumerCacheKey, jmsConsumer, s4JJMSContextWrapper);
            }

            jmsConsumers.put(consumerCacheKey, jmsConsumer);
        }

        return jmsConsumer;
    }

    protected boolean commitTransaction(int txnBatchNum, int jmsSessionMode, long curCycleNum) {
        // Whether to commit the transaction which happens when:
        // - session mode is equal to "SESSION_TRANSACTED"
        // - "txn_batch_num" has been reached since last reset
        boolean commitTransaction = ( (Session.SESSION_TRANSACTED == jmsSessionMode) && (txnBatchNum > 0) );
        if (commitTransaction) {
            int txnBatchTackingCnt = s4jSpace.getTxnBatchTrackingCnt();

            if ( ( (txnBatchTackingCnt > 0) && ((txnBatchTackingCnt % txnBatchNum) == 0) ) ||
                 ( curCycleNum >= (totalCycleNum - 1) ) ) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Commit transaction ({}, {}, {})",
                        txnBatchTackingCnt,
                        s4jSpace.getTotalOpResponseCnt(), curCycleNum);
                }
            }
            else {
                commitTransaction = false;
            }

            s4jSpace.incTxnBatchTrackingCnt();
        }

        return !commitTransaction;
    }
}
