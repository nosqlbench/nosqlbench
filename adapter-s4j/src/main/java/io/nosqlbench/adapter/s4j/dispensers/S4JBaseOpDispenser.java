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

import com.datastax.oss.pulsar.jms.PulsarJMSContext;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.ops.S4JOp;
import io.nosqlbench.adapter.s4j.util.*;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract  class S4JBaseOpDispenser extends BaseOpDispenser<S4JOp, S4JSpace> {

    private static final Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final S4JSpace s4jSpace;
    protected final S4JAdapterMetrics s4jAdapterMetrics;

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

    protected S4JBaseOpDispenser(DriverAdapter adapter,
                                 ParsedOp op,
                                 LongFunction<String> destNameStrFunc,
                                 S4JSpace s4jSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.s4jSpace = s4jSpace;

        this.s4jAdapterMetrics = new S4JAdapterMetrics(this);
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

        this.totalThreadNum = NumberUtils.toInt(parsedOp.getStaticConfig("threads", String.class));
        this.totalCycleNum = NumberUtils.toLong(parsedOp.getStaticConfig("cycles", String.class));
        s4jSpace.setTotalCycleNum(totalCycleNum);
    }

    public S4JSpace getS4jSpace() { return s4jSpace; }
    public S4JAdapterMetrics getS4jAdapterMetrics() { return s4jAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(String paramName, boolean defaultValue) {
        LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = l -> parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(BooleanUtils::toBoolean)
            .orElse(defaultValue);
        logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(String paramName) {
        LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = l -> parsedOp.getOptionalStaticValue(paramName, String.class)
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
        integerLongFunction = l -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(NumberUtils::toInt)
            .map(value -> {
                if (0 > value) return 0;
                return value;
            }).orElse(defaultValue);
        logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
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

    public S4JJMSContextWrapper getS4jJmsContextWrapper(long curCycle) {
        return getS4jJmsContextWrapper(curCycle, null);
    }

    // Get the next JMSContext Wrapper in the following approach
    // - The JMSContext wrapper pool has the following sequence (assuming 3 [c]onnections and 2 [s]essions per connection):
    //   c0s0, c0s1, c1s0, c1s1, c2s0, c2s1
    // - When getting the next JMSContext wrapper, always get from the next connection, starting from the first session
    //   When reaching the end of connection, move back to the first connection, but get the next session.
    //   e.g. first: c0s0   (0)
    //        next:  c1s0   (1)
    //        next:  c2s0   (2)
    //        next:  c0s1   (3)
    //        next:  c1s1   (4)
    //        next:  c2s1   (5)
    //        next:  c0s0   (6)  <-- repeat the pattern
    //        next:  c1s0   (7)
    //        next:  c2s0   (8)
    //        next:  c0s1   (9)
    //        ... ...
    public S4JJMSContextWrapper getS4jJmsContextWrapper(
        long curCycle,
        Map<String, Object> overrideS4jConfMap)
    {
        int totalConnNum = s4jSpace.getMaxNumConn();
        int totalSessionPerConnNum = s4jSpace.getMaxNumSessionPerConn();

        int connSeqNum =  (int) curCycle % totalConnNum;
        int sessionSeqNum = ( (int)(curCycle / totalConnNum) ) % totalSessionPerConnNum;

        JMSContext connLvlJmsContext = s4jSpace.getConnLvlJMSContext(s4jSpace.getConnLvlJmsContextIdentifier(connSeqNum));
        // Connection level JMSContext objects should be already created during the initialization phase
        assert (connLvlJmsContext != null);

        String jmsSessionContextIdStr = s4jSpace.getSessionLvlJmsContextIdentifier(connSeqNum, sessionSeqNum);
        S4JSpace.JMSGenObjCacheKey jmsContextWrapperCacheKey =
            new S4JSpace.JMSGenObjCacheKey(jmsSessionContextIdStr);

        return s4jSpace.getS4JJMSContextWrapper(jmsContextWrapperCacheKey, () -> {
            JMSContext jmsContext = null;

            if (overrideS4jConfMap == null || overrideS4jConfMap.isEmpty()) {
                jmsContext = connLvlJmsContext.createContext(connLvlJmsContext.getSessionMode());
            } else {
                jmsContext = ((PulsarJMSContext) connLvlJmsContext).createContext(
                    connLvlJmsContext.getSessionMode(), overrideS4jConfMap);
            }

            S4JJMSContextWrapper s4JJMSContextWrapper =
                new S4JJMSContextWrapper(jmsSessionContextIdStr, jmsContext);

            if (logger.isDebugEnabled()) {
                logger.debug("[Session level JMSContext] {} -- {}",
                    Thread.currentThread().getName(),
                    s4JJMSContextWrapper);
            }

            return s4JJMSContextWrapper;
        });
    }

    /**
     * If the JMS destination that corresponds to a topic exists, reuse it; Otherwise, create it
     */
    public Destination getJmsDestination(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        boolean tempDest,
        String destType,
        String destName) throws JMSRuntimeException
    {
        String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        S4JSpace.JMSDestinationCacheKey destinationCacheKey =
            new S4JSpace.JMSDestinationCacheKey(jmsContextIdStr, destType, destName);

        return s4jSpace.getJmsDestination(destinationCacheKey, () -> {
            Destination destination;

           // Regular, non-temporary destination
            if (!tempDest) {
                if (StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.QUEUE.label)) {
                    destination = jmsContext.createQueue(destName);
                } else {
                    destination = jmsContext.createTopic(destName);
                }
            }
            // Temporary destination
            else {
                if (StringUtils.equalsIgnoreCase(destType, S4JAdapterUtil.JMS_DEST_TYPES.QUEUE.label)) {
                    destination = jmsContext.createTemporaryQueue();
                }
                else {
                    destination = jmsContext.createTemporaryTopic();
                }
            }

            return destination;
        });
    }

    // Get simplified NB thread name
    private String getSimplifiedNBThreadName(String fullThreadName) {
        assert StringUtils.isNotBlank(fullThreadName);
        if (StringUtils.contains(fullThreadName, '/'))
            return StringUtils.substringAfterLast(fullThreadName, "/");
        else
            return fullThreadName;
    }

    /**
     * If the JMS producer that corresponds to a destination exists, reuse it; Otherwise, create it
     */
    public JMSProducer getJmsProducer(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        boolean asyncApi) throws JMSException
    {
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        S4JSpace.JMSGenObjCacheKey producerCacheKey =
            new S4JSpace.JMSGenObjCacheKey(
                String.join("::",
            getSimplifiedNBThreadName(Thread.currentThread().getName()), "producer"));

        return s4jSpace.getJmsProducer(producerCacheKey, () -> {
            JMSProducer jmsProducer = jmsContext.createProducer();

            if (asyncApi) {
                jmsProducer.setAsync(new S4JCompletionListener(s4jSpace, this));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Producer created: {} -- {} -- {}",
                    producerCacheKey, jmsProducer, s4JJMSContextWrapper);
            }

            return jmsProducer;
        });
    }

    /**
     * If the JMS consumer that corresponds to a destination(, subscription, message selector) exists, reuse it; Otherwise, create it
     */
    public JMSConsumer getJmsConsumer(
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

        S4JSpace.JMSGenObjCacheKey consumerCacheKey =
            new S4JSpace.JMSGenObjCacheKey(
                String.join("::",
                    getSimplifiedNBThreadName(Thread.currentThread().getName()), "consumer"));

        return s4jSpace.getJmsConsumer(consumerCacheKey, () -> {
            JMSConsumer jmsConsumer;

            if (isTopic) {
                if (!durable && !shared)
                    jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);
                else {
                    if (StringUtils.isBlank(subName)) {
                        throw new RuntimeException("Subscription name is required for receiving messages from a durable or shared topic!");
                    }

                    if (durable && !shared)
                        jmsConsumer = jmsContext.createDurableConsumer((Topic) destination, subName, msgSelector, nonLocal);
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

            return jmsConsumer;
        });
    }

    protected boolean commitTransaction(int txnBatchNum, int jmsSessionMode, long curCycleNum) {
        // Whether to commit the transaction which happens when:
        // - session mode is equal to "SESSION_TRANSACTED"
        // - "txn_batch_num" has been reached since last reset
        boolean commitTransaction = (Session.SESSION_TRANSACTED == jmsSessionMode) && (0 < txnBatchNum);
        if (commitTransaction) {
            int txnBatchTackingCnt = s4jSpace.getTxnBatchTrackingCnt();

            if (((0 < txnBatchTackingCnt) && (0 == (txnBatchTackingCnt % txnBatchNum))) ||
                (curCycleNum >= (totalCycleNum - 1))) {
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

        return commitTransaction;
    }
}
