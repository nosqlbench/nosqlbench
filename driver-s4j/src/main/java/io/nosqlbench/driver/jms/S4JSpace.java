package io.nosqlbench.driver.jms;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.jms.conn.S4JConnInfo;
import io.nosqlbench.driver.jms.excption.S4JDriverUnexpectedException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JJMSContextWrapper;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * An instance of a pulsar client, along with all the cached objects which are normally
 * associated with it during a client session in a typical application.
 * A S4JSpace is simply a named and cached set of objects which must be used together.
 */
public class S4JSpace {

    private final static Logger logger = LogManager.getLogger(S4JSpace.class);

    private final String spaceName;

    private CommandTemplate cmdTpl;

    private AtomicLong totalOpResponseCnt = new AtomicLong(0);

    // Represents the JMS connection
    private PulsarConnectionFactory s4jConnFactory;

    // - Each S4J space currently represents one JMS connection which could have
    //   multiple JMS sessions. Multi-connection scenario can be achieved using
    //   multiple NB processes.
    // - Each JMS session has its own sets of JMS destinations, producers, consumers, etc.
    private final ConcurrentHashMap<String, S4JJMSContextWrapper> jmsContexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSProducer> jmsProducers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSConsumer> jmsConsumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QueueBrowser> queueBrowsers = new ConcurrentHashMap<>();

    // Keep track the transaction count per thread
    private ThreadLocal<Integer> txnBatchTrackingCnt = ThreadLocal.withInitial(() -> {
        return Integer.valueOf(0);
    });

    private final S4JActivity s4JActivity;
    private final ActivityDef activityDef;

    public S4JSpace(String name, S4JActivity s4JActivity) {
        this.spaceName = name;
        this.s4JActivity = s4JActivity;
        this.activityDef = s4JActivity.getActivityDef();
    }

    public CommandTemplate getCmdTpl() { return this.cmdTpl; }
    public void setCmdTpl(CommandTemplate cmdTpl) { this.cmdTpl = cmdTpl; }

    // When completing NB execution, don't shut down right away because otherwise, async operation processing may fail.
    // Instead, shut down when either one of the following condition is satisfied
    // 1) the total number of the received operation response is the same as the total number of operations being executed;
    // 2) time has passed for 1 minutes
    private void waitUntilAllOpfinished(String stmtOpType, Instant shutdownStartTime) {
        long totalCycleNum = this.s4JActivity.getActivityDef().getEndCycle();
        long totalResponseCnt = 0;

        long timeElapsed = 0;
        do {
            Instant curTime = Instant.now();
            timeElapsed = Duration.between(shutdownStartTime, curTime).toSeconds();
            totalResponseCnt = this.getTotalOpResponseCnt();

            // Sleep for 1 second
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while ((totalResponseCnt < totalCycleNum) && (timeElapsed <= 60));

        if (logger.isDebugEnabled()) {
            logger.debug("Total operation (type: {}) response received: {}. Total cycle number: {}",
                stmtOpType,
                totalResponseCnt,
                totalCycleNum);
        }
    }

    // Properly shut down all Pulsar objects (producers, consumers, etc.) that are associated with this space
    public void shutdownSpace() {
        try {
            // for message send and receive operations, async processing is possible
            String stmtOpType = this.cmdTpl.getStatic("optype");
            if (StringUtils.equalsAnyIgnoreCase(stmtOpType,
                S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label,
                S4JActivityUtil.MSG_OP_TYPES.MSG_READ.label,
                S4JActivityUtil.MSG_OP_TYPES.MSG_READ_DURABLE.label,
                S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED.label,
                S4JActivityUtil.MSG_OP_TYPES.MSG_READ_SHARED_DURABLE.label)) {
                this.waitUntilAllOpfinished(stmtOpType, Instant.now());
            }

            this.txnBatchTrackingCnt.remove();

            for (QueueBrowser queueBrowser : queueBrowsers.values()) {
                if (queueBrowser != null) queueBrowser.close();
            }

            for (JMSConsumer jmsConsumer : jmsConsumers.values()) {
                if (jmsConsumer != null) jmsConsumer.close();
            }

            for (S4JJMSContextWrapper s4JJMSContextWrapper : jmsContexts.values()) {
                if (s4JJMSContextWrapper != null) {
                    JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
                    jmsContext.close();
                }
            }
        }
        catch (JMSException e) {
            throw new RuntimeException("Unexpected error when shutting down S4JSpace!");
        }
    }

    public int getTxnBatchTrackingCnt() {
        return txnBatchTrackingCnt.get();
    }

    public void incTxnBatchTrackingCnt() {
        int curVal = getTxnBatchTrackingCnt();
        txnBatchTrackingCnt.set(Integer.valueOf(curVal + 1));
    }

    public long getTotalOpResponseCnt() {
        return totalOpResponseCnt.get();
    }
    public long incTotalOpResponseCnt() {
        return totalOpResponseCnt.incrementAndGet();
    }
    public void resetTotalOpResponseCnt() {
        totalOpResponseCnt.set(0);
    }

    private String getJmsContextIdentifier(int jmsSessionSeqNum) {
        return S4JActivityUtil.buildCacheKey(
            this.spaceName,
            this.s4jConnFactory.toString(),
            StringUtils.join("session-", jmsSessionSeqNum));
    }

    public void initializeS4JConnectionFactory(S4JConnInfo s4JConnInfo) {
        if (s4jConnFactory == null) {
            Map<String, Object> cfgMap = new HashMap<>();
            try {
                cfgMap = s4JConnInfo.getS4jConfMap();
                s4jConnFactory = new PulsarConnectionFactory(cfgMap);

                int sessionMode = s4JConnInfo.getSessionMode();

                // Establish one JMS connection
                JMSContext jmsContext = null;
                if (sessionMode == -1)
                    jmsContext = s4jConnFactory.createContext();
                else
                    jmsContext = s4jConnFactory.createContext(sessionMode);

                jmsContext.setExceptionListener(e -> {
                    if (logger.isDebugEnabled()) {
                        logger.error("onException::Unexpected JMS error happened:" + e.getMessage());
                    }
                });

                String jmsContextIdStr = getJmsContextIdentifier(0);
                jmsContexts.put(jmsContextIdStr, new S4JJMSContextWrapper(jmsContextIdStr, jmsContext));

                // The rest of the JMSContexts share the same JMS connection, but using separate JMS sessions
                for (int i = 1; i < s4JActivity.getMaxNumSessionPerConn(); i++) {
                    JMSContext newSessionContext = jmsContext.createContext(jmsContext.getSessionMode());
                    jmsContextIdStr = getJmsContextIdentifier(i);
                    jmsContexts.put(jmsContextIdStr, new S4JJMSContextWrapper(jmsContextIdStr, newSessionContext));
                }
            } catch (JMSRuntimeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[ERROR] Unable to initialize JMS connection factory with the following configuration parameters: {}", s4JConnInfo.toString());
                }
                throw new S4JDriverUnexpectedException("Unable to initialize JMS connection factory with the following error message: " + e.getMessage());
            }
        }
    }

    public PulsarConnectionFactory getS4jConnFactory() { return s4jConnFactory; }

    public S4JJMSContextWrapper getS4jJmsContextWrapper(String identifier) {
        return jmsContexts.get(identifier);
    }
    public S4JJMSContextWrapper getS4jJmsContextWrapper(int jmsSessionSeqNum) {
        String jmsContextIdStr = getJmsContextIdentifier(jmsSessionSeqNum);
        return getS4jJmsContextWrapper(jmsContextIdStr);
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
            String destinationCacheKey = S4JActivityUtil.buildCacheKey(jmsContextIdStr, destType, destName);
            Destination destination = jmsDestinations.get(destinationCacheKey);

            if (destination == null) {
                if (StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.QUEUE.label)) {
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
            if (StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.QUEUE.label)) {
                return jmsContext.createTemporaryQueue();
            } else  {
                return jmsContext.createTemporaryTopic();
            }
        }
    }

    /**
     * If the JMS producer that corresponds to a destination exists, reuse it; Otherwise, create it
     */
    public JMSProducer getOrCreateJmsProducer(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        Destination destination,
        String destType,
        boolean reuseClnt,
        boolean asyncApi) throws JMSException
    {
        String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        String destName = S4JActivityUtil.getDestinationName(destination, destType);

        String producerCacheKey = S4JActivityUtil.buildCacheKey(jmsContextIdStr, "producer");;
        if (!reuseClnt)
            producerCacheKey = S4JActivityUtil.buildCacheKey(jmsContextIdStr, "producer", destType, destName);

        JMSProducer jmsProducer = jmsProducers.get(producerCacheKey);

        if (jmsProducer == null) {
            jmsProducer = jmsContext.createProducer();

            if (asyncApi) {
                jmsProducer.setAsync(new CompletionListener() {
                    @Override
                    public void onCompletion(Message message) {
                        try {
                            if (logger.isDebugEnabled()) {
                                // for testing purpose
                                String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);

                                logger.debug("onCompletion::Async message send successful - message ID {} ({}) "
                                    , message.getJMSMessageID(), myMsgSeq);
                            }

                            long totalResponseCnt = incTotalOpResponseCnt();
                            if (logger.isTraceEnabled()) {
                                logger.trace("... async op response received so far: {}", totalResponseCnt);
                            }
                        }
                        catch (JMSException jmsException) {
                            logger.warn("onCompletion::Error retrieving message property - {}", jmsException.getMessage());
                        }
                    }

                    @Override
                    public void onException(Message message, Exception e) {
                        try {
                            if (logger.isDebugEnabled()) {
                                // for testing purpose
                                String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);

                                logger.debug("onException::Async message send failed - message ID {} ({}) "
                                    , message.getJMSMessageID(), myMsgSeq);
                            }
                        }
                        catch (JMSException jmsException) {
                            logger.warn("onException::Unexpected error: " + jmsException.getMessage());
                        }
                    }
                });
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
        boolean nonLocal,
        boolean durable,
        boolean shared,
        boolean reuseClnt,
        boolean asyncApi) throws JMSException
    {
        String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        boolean isTopic = StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label);
        String destName = S4JActivityUtil.getDestinationName(destination, destType);

        String consumerCacheKey = S4JActivityUtil.buildCacheKey(jmsContextIdStr, "consumer");
        if (!reuseClnt) {
            if (isTopic) {
                consumerCacheKey = S4JActivityUtil.buildCacheKey(
                    jmsContextIdStr,
                    "consumer",
                    destType,
                    destName,
                    msgSelector,
                    String.valueOf(nonLocal),
                    String.valueOf(durable),
                    String.valueOf(shared)
                );
            } else {
                consumerCacheKey = S4JActivityUtil.buildCacheKey(
                    jmsContextIdStr,
                    "consumer",
                    destType,
                    destName,
                    msgSelector,
                    String.valueOf(nonLocal)
                );
            }
        }

        JMSConsumer jmsConsumer = jmsConsumers.get(consumerCacheKey);
        if (jmsConsumer == null) {
            if (isTopic) {
                if (!durable && !shared)
                    jmsConsumer = jmsContext.createConsumer(destination, msgSelector, nonLocal);
                else {
                    if (StringUtils.isBlank(subName)) {
                        throw new RuntimeException("Subscription name is required for receiving messages from a durable or shared topic!");
                    }

                    if (durable) {
                        String clientID = jmsContext.getClientID();
                        if (StringUtils.isBlank(clientID)) {
                            clientID = Base64.getEncoder().encodeToString(consumerCacheKey.getBytes());
                            try {
                                jmsContext.setClientID(clientID);
                            }
                            catch (JMSRuntimeException jre) {
                                jre.printStackTrace();
                                //throw new RuntimeException("Unable to set Client ID properly for a durable subscription!");
                            }
                        }
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
                jmsConsumer.setMessageListener(message -> {
                    try {
                        int msgSize = message.getIntProperty(S4JActivityUtil.NB_MSG_SIZE_PROP);
                        Counter bytesCounter = this.s4JActivity.getBytesCounter();
                        bytesCounter.inc(msgSize);
                        Histogram messageSizeHistogram = this.s4JActivity.getMessagesizeHistogram();
                        messageSizeHistogram.update(msgSize);

                        if (logger.isDebugEnabled()) {
                            // for testing purpose
                            String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);

                            logger.debug("onMessage::Async message receive successful - message ID {} ({}) "
                                , message.getJMSMessageID(), myMsgSeq);
                        }

                        incTotalOpResponseCnt();
                    }
                    catch (JMSException jmsException) {
                        logger.warn("onMessage::Unexpected error:" + jmsException.getMessage());
                    }
                });
            }

            jmsConsumers.put(consumerCacheKey, jmsConsumer);
        }

        return jmsConsumer;
    }

    /**
     * If the JMS QueueBrowser that corresponds to a destination(, message selector) exists, reuse it; Otherwise, create it
     */
    public QueueBrowser getOrCreateJmsQueueBrowser(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        Queue queue,
        String msgSelector) throws JMSException
    {
        String jmsContextIdStr = s4JJMSContextWrapper.getJmsContextIdentifer();
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        String queueBrowserCacheKey = S4JActivityUtil.buildCacheKey(jmsContextIdStr, "queue_browser", msgSelector);

        QueueBrowser queueBrowser = queueBrowsers.get(queueBrowserCacheKey);

        if (queueBrowser == null) {
            queueBrowser = jmsContext.createBrowser(queue, msgSelector);
            queueBrowsers.put(queueBrowserCacheKey, queueBrowser);
        }

        return queueBrowser;
    }
}
