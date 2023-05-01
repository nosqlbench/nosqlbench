/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.adapter.s4j.dispensers;

import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.ops.S4JOp;
import io.nosqlbench.adapter.s4j.util.*;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil.JMS_DEST_TYPES;
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

    private static final Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

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

    protected S4JBaseOpDispenser(final DriverAdapter adapter,
                                 final ParsedOp op,
                                 final LongFunction<String> destNameStrFunc,
                                 final S4JSpace s4jSpace) {

        super(adapter, op);

        parsedOp = op;
        this.s4jSpace = s4jSpace;
        connLvlJmsContexts.putAll(s4jSpace.getConnLvlJmsContexts());
        sessionLvlJmsContexts.putAll(s4jSpace.getSessionLvlJmsContexts());

        final String defaultMetricsPrefix = this.parsedOp.linearizeLabels();
        s4jAdapterMetrics = new S4JAdapterMetrics(defaultMetricsPrefix);
        this.s4jAdapterMetrics.initS4JAdapterInstrumentation();

        this.destNameStrFunc = destNameStrFunc;
        temporaryDest =
            this.parsedOp.getStaticConfigOr(DOC_LEVEL_PARAMS.TEMP_DEST.label, Boolean.FALSE);
        destType =
            this.parsedOp.getStaticConfig(DOC_LEVEL_PARAMS.DEST_TYPE.label, String.class);
        asyncAPI =
            this.parsedOp.getStaticConfigOr(DOC_LEVEL_PARAMS.ASYNC_API.label, Boolean.TRUE);
        txnBatchNum =
            this.parsedOp.getStaticConfigOr(DOC_LEVEL_PARAMS.TXN_BATCH_NUM.label, Integer.valueOf(0));

        totalThreadNum = NumberUtils.toInt(this.parsedOp.getStaticConfig("threads", String.class));
        totalCycleNum = NumberUtils.toLong(this.parsedOp.getStaticConfig("cycles", String.class));
        s4jSpace.setTotalCycleNum(this.totalCycleNum);
    }

    public S4JSpace getS4jSpace() { return this.s4jSpace; }
    public S4JAdapterMetrics getS4jAdapterMetrics() { return this.s4jAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(final String paramName, final boolean defaultValue) {
        final LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = l -> this.parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        S4JBaseOpDispenser.logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(final String paramName) {
        final LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = l -> this.parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<String > set = new HashSet<>();

                if (StringUtils.contains(value,',')) set = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(Predicate.not(String::isEmpty))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

                return set;
            }).orElse(Collections.emptySet());
        S4JBaseOpDispenser.logger.info("{}: {}", paramName, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<Integer> lookupStaticIntOpValueFunc(final String paramName, final int defaultValue) {
        final LongFunction<Integer> integerLongFunction;
        integerLongFunction = l -> this.parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> NumberUtils.toInt(value))
            .map(value -> {
                if (0 > value) {
                    return 0;
                }
                return value;
            }).orElse(defaultValue);
        S4JBaseOpDispenser.logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName, final String defaultValue) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
        S4JBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName) {
        return this.lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(final String paramName) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsRequiredFunction(paramName, String.class);
        S4JBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    /**
     * If the JMS destination that corresponds to a topic exists, reuse it; Otherwise, create it
     */
    public Destination getOrCreateJmsDestination(
        final S4JJMSContextWrapper s4JJMSContextWrapper,
        final boolean tempDest,
        final String destType,
        final String destName) throws JMSRuntimeException
    {
        final String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        final JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        // Regular, non-temporary destination
        if (!tempDest) {
            final String destinationCacheKey = S4JAdapterUtil.buildCacheKey(jmsContextIdStr, destType, destName);
            Destination destination = this.jmsDestinations.get(destinationCacheKey);

            if (null == destination) {
                if (StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.QUEUE.label))
                    destination = jmsContext.createQueue(destName);
                else destination = jmsContext.createTopic(destName);

                this.jmsDestinations.put(destinationCacheKey, destination);
            }

            return destination;
        }
        // Temporary destination

        if (StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.QUEUE.label))
            return jmsContext.createTemporaryQueue();
        return jmsContext.createTemporaryTopic();
    }

    // Get simplified NB thread name
    private String getSimplifiedNBThreadName(final String fullThreadName) {
        assert StringUtils.isNotBlank(fullThreadName);

        if (StringUtils.contains(fullThreadName, '/')) {
            return StringUtils.substringAfterLast(fullThreadName, "/");
        }
        return fullThreadName;
    }


    /**
     * If the JMS producer that corresponds to a destination exists, reuse it; Otherwise, create it
     */
    public JMSProducer getOrCreateJmsProducer(
        final S4JJMSContextWrapper s4JJMSContextWrapper,
        final boolean asyncApi) throws JMSException
    {
        final JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        final String producerCacheKey = S4JAdapterUtil.buildCacheKey(
            this.getSimplifiedNBThreadName(Thread.currentThread().getName()), "producer");
        JMSProducer jmsProducer = this.jmsProducers.get(producerCacheKey);

        if (null == jmsProducer) {
            jmsProducer = jmsContext.createProducer();

            if (asyncApi) jmsProducer.setAsync(new S4JCompletionListener(this.s4jSpace, this));

            if (S4JBaseOpDispenser.logger.isDebugEnabled())
                S4JBaseOpDispenser.logger.debug("Producer created: {} -- {} -- {}",
                    producerCacheKey, jmsProducer, s4JJMSContextWrapper);

            this.jmsProducers.put(producerCacheKey, jmsProducer);
        }

        return jmsProducer;
    }

    /**
     * If the JMS consumer that corresponds to a destination(, subscription, message selector) exists, reuse it; Otherwise, create it
     */
    public JMSConsumer getOrCreateJmsConsumer(
        final S4JJMSContextWrapper s4JJMSContextWrapper,
        final Destination destination,
        final String destType,
        final String subName,
        final String msgSelector,
        final float msgAckRatio,
        final boolean nonLocal,
        final boolean durable,
        final boolean shared,
        final boolean asyncApi,
        final int slowAckInSec) throws JMSException
    {
        final JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        final boolean isTopic = StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.TOPIC.label);
        final String consumerCacheKey = S4JAdapterUtil.buildCacheKey(
            this.getSimplifiedNBThreadName(Thread.currentThread().getName()), "consumer");

        JMSConsumer jmsConsumer = this.jmsConsumers.get(consumerCacheKey);
        if (null == jmsConsumer) {
            if (isTopic) if (!durable && !shared) {
                jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);
            } else {
                if (StringUtils.isBlank(subName))
                    throw new RuntimeException("Subscription name is required for receiving messages from a durable or shared topic!");

                if (durable && !shared) {
                    jmsConsumer = jmsContext.createDurableConsumer(
                        (Topic) destination, subName, msgSelector, nonLocal);
                } else if (!durable) {
                    jmsConsumer = jmsContext.createSharedConsumer((Topic) destination, subName, msgSelector);
                } else {
                    jmsConsumer = jmsContext.createSharedDurableConsumer((Topic) destination, subName, msgSelector);
                }
            }
            else jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);

            if (asyncApi) jmsConsumer.setMessageListener(
                new S4JMessageListener(jmsContext, this.s4jSpace, this, msgAckRatio, slowAckInSec));

            if (S4JBaseOpDispenser.logger.isDebugEnabled())
                S4JBaseOpDispenser.logger.debug("Consumer created: {} -- {} -- {}",
                    consumerCacheKey, jmsConsumer, s4JJMSContextWrapper);

            this.jmsConsumers.put(consumerCacheKey, jmsConsumer);
        }

        return jmsConsumer;
    }

    protected boolean commitTransaction(final int txnBatchNum, final int jmsSessionMode, final long curCycleNum) {
        // Whether to commit the transaction which happens when:
        // - session mode is equal to "SESSION_TRANSACTED"
        // - "txn_batch_num" has been reached since last reset
        boolean commitTransaction = Session.SESSION_TRANSACTED == jmsSessionMode && 0 < txnBatchNum;
        if (commitTransaction) {
            final int txnBatchTackingCnt = this.s4jSpace.getTxnBatchTrackingCnt();

            if ( 0 < txnBatchTackingCnt && 0 == txnBatchTackingCnt % txnBatchNum ||
                curCycleNum >= this.totalCycleNum - 1) {
                if (S4JBaseOpDispenser.logger.isDebugEnabled())
                    S4JBaseOpDispenser.logger.debug("Commit transaction ({}, {}, {})",
                        txnBatchTackingCnt,
                        this.s4jSpace.getTotalOpResponseCnt(), curCycleNum);
            }
            else commitTransaction = false;

            this.s4jSpace.incTxnBatchTrackingCnt();
        }

        return !commitTransaction;
    }
}
