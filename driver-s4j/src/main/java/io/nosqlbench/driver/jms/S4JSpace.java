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



import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.jms.conn.S4JConnInfo;
import io.nosqlbench.driver.jms.excption.S4JDriverParamException;
import io.nosqlbench.driver.jms.excption.S4JDriverUnexpectedException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JCompletionListener;
import io.nosqlbench.driver.jms.util.S4JJMSContextWrapper;
import io.nosqlbench.driver.jms.util.S4JMessageListener;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

    // Total number of acknowledgement received
    // - this can apply to both message production and consumption
    // - for message consumption, this only applies to non-null messages received (which is for async API)
    private final AtomicLong totalOpResponseCnt = new AtomicLong(0);
    // Total number of null messages received
    // - only applicable to message consumption
    private final AtomicLong nullMsgRecvCnt = new AtomicLong(0);

    // Represents the JMS connection
    private PulsarConnectionFactory s4jConnFactory;

    // - Each S4J space currently represents a number of JMS connections (\"num_conn\" NB CLI parameter);
    // - JMS connection can have a number of JMS sessions (\"num_session\" NB CLI parameter).
    // - Each JMS session has its own sets of JMS destinations, producers, consumers, etc.
    private final ConcurrentHashMap<String, S4JJMSContextWrapper> jmsContexts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSProducer> jmsProducers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSConsumer> jmsConsumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QueueBrowser> queueBrowsers = new ConcurrentHashMap<>();

    // Keep track the transaction count per thread
    private final ThreadLocal<Integer> txnBatchTrackingCnt = ThreadLocal.withInitial(() -> 0);

    private final S4JActivity s4JActivity;
    private final ActivityDef activityDef;

    public S4JSpace(String name, S4JActivity s4JActivity) {
        this.spaceName = name;
        this.s4JActivity = s4JActivity;
        this.activityDef = s4JActivity.getActivityDef();
    }

    public S4JActivity getS4JActivity() { return this.s4JActivity; }

    public CommandTemplate getCmdTpl() { return this.cmdTpl; }
    public void setCmdTpl(CommandTemplate cmdTpl) { this.cmdTpl = cmdTpl; }

    private String buildExecSummaryString(
        long totalCycleNum,
        String stmtOpType,
        boolean trackingMsgCnt,
        long timeElapsedMills,
        long totalResponseCnt,
        long totalNullMsgCnt)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
            .append("shutdownSpace::waitUntilAllOpFinished -- ")
            .append("cycle number: ").append(totalCycleNum).append("; ")
            .append("operation type: ").append(stmtOpType).append("; ")
            .append("shutdown time elapsed: ").append(timeElapsedMills).append("ms; ");

        if (trackingMsgCnt) {
            stringBuilder.append("response received: ").append(totalResponseCnt).append("; ");
            if (S4JActivityUtil.isMsgReadOpType(stmtOpType)) {
                stringBuilder.append("null msg received: ").append(totalNullMsgCnt).append("; ");
            }
        }

        return stringBuilder.toString();
    }

    // When completing NB execution, don't shut down right away because otherwise, async operation processing may fail.
    // Instead, shut down when either one of the following condition is satisfied
    // 1) the total number of the received operation response is the same as the total number of operations being executed;
    // 2) time has passed for 30 seconds
    private void waitUntilAllOpFinished(long shutdownStartTimeMills, String stmtOpType) {
        long totalCycleNum = this.activityDef.getEndCycle();
        long totalResponseCnt = 0;
        long totalNullMsgCnt = 0;
        long timeElapsedMills = 0;

        boolean trackingMsgCnt = this.s4JActivity.isTrackingMsgRecvCnt();
        boolean continueChk;

        do {
            // Sleep for 2 second
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new S4JDriverUnexpectedException(e);
            }

            long curTimeMills = System.currentTimeMillis();
            timeElapsedMills = curTimeMills - shutdownStartTimeMills;
            continueChk = (timeElapsedMills <= 30000);

            if (trackingMsgCnt) {
                totalResponseCnt = this.getTotalOpResponseCnt();
                totalNullMsgCnt = this.getTotalNullMsgRecvdCnt();
                continueChk = continueChk && (totalResponseCnt < totalCycleNum);
            }

            if (logger.isTraceEnabled()) {
                logger.trace(
                    buildExecSummaryString(
                        totalCycleNum, stmtOpType, trackingMsgCnt,
                        timeElapsedMills, totalResponseCnt, totalNullMsgCnt));
            }
        } while (continueChk);

        logger.info(
            buildExecSummaryString(
                totalCycleNum, stmtOpType, trackingMsgCnt,
                timeElapsedMills, totalResponseCnt, totalNullMsgCnt));
    }

    // Properly shut down all Pulsar objects (producers, consumers, etc.) that are associated with this space
    public void shutdownSpace() {
        long shutdownStartTimeMills = System.currentTimeMillis();

        try {
            // for message send and receive operations, async processing is possible
            String stmtOpType = this.cmdTpl.getStatic("optype");
            if ( S4JActivityUtil.isMsgSendOpType(stmtOpType) ||
                 S4JActivityUtil.isMsgReadOpType(stmtOpType) ) {
                waitUntilAllOpFinished(shutdownStartTimeMills, stmtOpType);
            }

            this.txnBatchTrackingCnt.remove();

            String jmsConnContextIdStr;
            S4JJMSContextWrapper s4JJMSContextWrapper;
            for (int i=0; i< s4JActivity.getMaxNumConn(); i++) {
                for (int j = 1; j < s4JActivity.getMaxNumSessionPerConn(); j++) {
                    jmsConnContextIdStr = getJmsContextIdentifier(i,j);
                    s4JJMSContextWrapper = jmsContexts.get(jmsConnContextIdStr);
                    if (s4JJMSContextWrapper != null) s4JJMSContextWrapper.close();
                }

                jmsConnContextIdStr = getJmsContextIdentifier(i,0);
                s4JJMSContextWrapper = jmsContexts.get(jmsConnContextIdStr);
                if (s4JJMSContextWrapper != null) s4JJMSContextWrapper.close();
            }

            s4jConnFactory.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new S4JDriverUnexpectedException("Unexpected error when shutting down S4JSpace!");
        }
    }

    public int getTxnBatchTrackingCnt() {
        return txnBatchTrackingCnt.get();
    }

    public void incTxnBatchTrackingCnt() {
        int curVal = getTxnBatchTrackingCnt();
        txnBatchTrackingCnt.set(curVal + 1);
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

    public long getTotalNullMsgRecvdCnt() {
        return nullMsgRecvCnt.get();
    }
    public long incTotalNullMsgRecvdCnt() {
        return nullMsgRecvCnt.incrementAndGet();
    }
    public void resetTotalNullMsgRecvdCnt() { nullMsgRecvCnt.set(0); }

    private String getJmsContextIdentifier(int jmsConnSeqNum, int jmsSessionSeqNum) {
        return S4JActivityUtil.buildCacheKey(
            this.spaceName,
            StringUtils.join("conn-", jmsConnSeqNum),
            StringUtils.join("session-", jmsSessionSeqNum));
    }

    // Create JMSContext that represents a new JMS connection
    private JMSContext createConnLvlJMSContext(
        PulsarConnectionFactory s4jConnFactory,
        S4JConnInfo s4JConnInfo,
        int sessionMode)
    {
        boolean useCredentialsEnable = s4JConnInfo.isUseCredentialsEnabled();
        JMSContext jmsConnContext;

        if (!useCredentialsEnable)
            jmsConnContext = s4jConnFactory.createContext(sessionMode);
        else {
            String userName = s4JConnInfo.getCredentialUserName();
            String passWord = s4JConnInfo.getCredentialPassword();

            // Password must be in "token:<token vale>" format
            if (! StringUtils.startsWith(passWord, "token:")) {
                throw new S4JDriverParamException(
                    "When 'jms.useCredentialsFromCreateConnection' is enabled, " +
                        "the provided password must be in format 'token:<token_value_...> ");
            }

            jmsConnContext = s4jConnFactory.createContext(userName, passWord, sessionMode);
        }

        return jmsConnContext;
    }

    public void initializeS4JConnectionFactory(S4JConnInfo s4JConnInfo) {
        if (s4jConnFactory == null) {
            Map<String, Object> cfgMap;
            try {
                cfgMap = s4JConnInfo.getS4jConfMap();
                s4jConnFactory = new PulsarConnectionFactory(cfgMap);

                String curThreadName = Thread.currentThread().getName();
                int sessionMode = s4JConnInfo.getSessionMode();

                for (int i=0; i< s4JActivity.getMaxNumConn(); i++) {
                    // Establish a JMS connection
                    String jmsConnContextIdStr = getJmsContextIdentifier(i,0);
                    String clientIdStr = Base64.getEncoder().encodeToString(jmsConnContextIdStr.getBytes());

                    JMSContext jmsConnContext = createConnLvlJMSContext(s4jConnFactory, s4JConnInfo, sessionMode);
                    jmsConnContext.setClientID(clientIdStr);
                    jmsConnContext.setExceptionListener(e -> {
                        if (logger.isDebugEnabled()) {
                            logger.error("onException::Unexpected JMS error happened:" + e);
                        }
                    });

                    S4JJMSContextWrapper jmsContextWrapper = new S4JJMSContextWrapper(jmsConnContextIdStr, jmsConnContext);
                    jmsContexts.put(jmsConnContextIdStr, jmsContextWrapper);

                    if (logger.isDebugEnabled()) {
                        logger.debug("{} -- {}",
                            curThreadName,
                            jmsContextWrapper );
                    }

                    // The rest of the JMSContexts share the same JMS connection, but using separate JMS sessions
                    for (int j = 1; j < s4JActivity.getMaxNumSessionPerConn(); j++) {
                        String jmsSessionContextIdStr = getJmsContextIdentifier(i, j);
                        JMSContext jmsSessionContext = jmsConnContext.createContext(jmsConnContext.getSessionMode());

                        jmsContextWrapper = new S4JJMSContextWrapper(jmsSessionContextIdStr, jmsSessionContext);
                        jmsContexts.put(jmsSessionContextIdStr, jmsContextWrapper);

                        if (logger.isDebugEnabled()) {
                            logger.debug("{} -- {}",
                                curThreadName,
                                jmsContextWrapper);
                        }
                    }
                }
            } catch (JMSRuntimeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[ERROR] Unable to initialize JMS connection factory with the following configuration parameters: {}", s4JConnInfo.toString());
                }
                throw new S4JDriverUnexpectedException("Unable to initialize JMS connection factory with the following error message: " + e.getCause());
            }
        }
    }

    public PulsarConnectionFactory getS4jConnFactory() { return s4jConnFactory; }

    public S4JJMSContextWrapper getS4jJmsContextWrapper(String identifier) {
        return jmsContexts.get(identifier);
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
    public S4JJMSContextWrapper getNextS4jJmsContextWrapper(long curCycle) {
        int totalConnNum = s4JActivity.getMaxNumConn();
        int totalSessionPerConnNum = s4JActivity.getMaxNumSessionPerConn();

        int connSeqNum =  (int) curCycle % totalConnNum;
        int sessionSeqNum = ( (int)(curCycle / totalConnNum) ) % totalSessionPerConnNum;
        String jmsContextIdStr = getJmsContextIdentifier(connSeqNum, sessionSeqNum);

        return jmsContexts.get(jmsContextIdStr);
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
        String producerCacheKey = S4JActivityUtil.buildCacheKey(
            getSimplifiedNBThreadName(Thread.currentThread().getName()), "producer");
        JMSProducer jmsProducer = jmsProducers.get(producerCacheKey);

        if (jmsProducer == null) {
            jmsProducer = jmsContext.createProducer();

            if (asyncApi) {
                jmsProducer.setAsync(new S4JCompletionListener(this));
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
        boolean asyncApi) throws JMSException
    {
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean isTopic = StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label);
        String consumerCacheKey = S4JActivityUtil.buildCacheKey(
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
                    new S4JMessageListener(jmsContext, this, msgAckRatio));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Consumer created: {} -- {} -- {}",
                    consumerCacheKey, jmsConsumer, s4JJMSContextWrapper);
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
