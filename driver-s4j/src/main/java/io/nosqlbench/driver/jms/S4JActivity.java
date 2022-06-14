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
import com.codahale.metrics.Timer;
import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.jms.conn.S4JConnInfo;
import io.nosqlbench.driver.jms.ops.ReadyS4JOp;
import io.nosqlbench.driver.jms.ops.S4JOp;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JConf;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class S4JActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(S4JActivity.class);

    private final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSProducer> jmsProducers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, JMSConsumer> jmsConsumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QueueBrowser> queueBrowsers = new ConcurrentHashMap<>();

    private S4JConnInfo s4JConnInfo;
    private JMSContext jmsContext;
    private String curThreadName;

    private OpSequence<OpDispenser<S4JOp>> sequence;
    private volatile Throwable asyncOperationFailure;
    private NBErrorHandler errorhandler;

    private Counter bytesCounter;
    private Histogram messageSizeHistogram;
    private Timer bindTimer;
    private Timer executeTimer;

    public S4JActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void shutdownActivity() {
        // Wait for a short while to make sure all async message processing is complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.shutdownActivity();

        if (jmsContext != null) {
            jmsContext.close();
        }
    }

    @Override
    public void initActivity() {
        super.initActivity();

        String curThreadName = Thread.currentThread().getName();

        String s4jConfFile =
            activityDef.getParams().getOptionalString("config").orElse("config.properties");
        S4JConf s4JConf = new S4JConf(s4jConfFile);

        String webSvcUrl =
            activityDef.getParams().getOptionalString("web_url").orElse("http://localhost:8080");
        String pulsarSvcUrl =
            activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");

        String sessionModeStr =
            activityDef.getParams().getOptionalString("session_mode").orElse("");

        s4JConnInfo = new S4JConnInfo(webSvcUrl, pulsarSvcUrl, sessionModeStr, s4JConf);

        PulsarConnectionFactory factory;
        Map<String, Object> cfgMap = new HashMap<>();
        try {
            cfgMap = s4JConnInfo.getS4jConfMap();
            factory = new PulsarConnectionFactory(cfgMap);

            int sessionMode = s4JConnInfo.getSessionMode();

            if (sessionMode == -1)
                this.jmsContext = factory.createContext();
            else
                this.jmsContext = factory.createContext(sessionMode);

            this.jmsContext.setExceptionListener(e -> {
                if (logger.isDebugEnabled()) {
                    logger.error("onException::Unexpected JMS error happened:" + e.getMessage());
                }
            });

        } catch (JMSRuntimeException e) {
            throw new RuntimeException("Unable to initialize JMS connection factory with the following configuration parameters: " + cfgMap);
        }

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messageSizeHistogram = ActivityMetrics.histogram(activityDef, "messagesize");

        this.sequence = createOpSequence((ot) -> new ReadyS4JOp(ot, this));

        setDefaultsFromOpSequence(sequence);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );
    }

    /**
     * If the JMS destination that corresponds to a topic exists, reuse it; Otherwise, create it
     */
    public Destination getOrCreateJmsDestination(boolean tempDest, String destType, String destName) throws JMSRuntimeException {
        // Regular, non-temporary destination
        if (!tempDest) {
            String destinationCacheKey = S4JActivityUtil.buildCacheKey(curThreadName, destType, destName);
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
    public JMSProducer getOrCreateJmsProducer(Destination destination, String destType, boolean reuseProducer, boolean asyncApi) throws JMSException {

        String destName = S4JActivityUtil.getDestinationName(destination, destType);

        String producerCacheKey = S4JActivityUtil.buildCacheKey(curThreadName, "producer");
        if (!reuseProducer) producerCacheKey = S4JActivityUtil.buildCacheKey(curThreadName, "producer", destType, destName);

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
                                String myMsgSeq = message.getStringProperty("MyMsgSeq");

                                logger.debug("onCompletion::Async message send successful - message ID {} ({}) "
                                    , message.getJMSMessageID(), myMsgSeq);
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
                                String myMsgSeq = message.getStringProperty("MyMsgSeq");

                                logger.debug("onCompletion::Async message send failed - message ID {} ({}) "
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
        Destination destination,
        String destType,
        String subName,
        String msgSelector,
        boolean nonLocal,
        boolean durable,
        boolean shared,
        boolean asyncApi) throws JMSException {

        boolean isTopic = StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label);
        String destName = S4JActivityUtil.getDestinationName(destination, destType);


        String consumerCacheKey;
        if (isTopic) {
            consumerCacheKey = S4JActivityUtil.buildCacheKey(
                curThreadName,
                "consumer",
                destType,
                destName,
                msgSelector,
                String.valueOf(nonLocal),
                String.valueOf(durable),
                String.valueOf(shared)
            );
        }
        else {
            consumerCacheKey = S4JActivityUtil.buildCacheKey(
                curThreadName,
                "consumer",
                destType,
                destName,
                msgSelector,
                String.valueOf(nonLocal)
            );
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
                        if (logger.isDebugEnabled()) {
                            // for testing purpose
                            String myMsgSeq = message.getStringProperty("MyMsgSeq");

                            logger.debug("onMessage::Async message receive successful - message ID {} ({}) "
                                , message.getJMSMessageID(), myMsgSeq);
                        }
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
    public QueueBrowser getOrCreateJmsQueueBrowser(Queue queue, String msgSelector) throws JMSException {
        String queueBrowserCacheKey = S4JActivityUtil.buildCacheKey(curThreadName, "queue_browser", msgSelector);

        QueueBrowser queueBrowser = queueBrowsers.get(queueBrowserCacheKey);

        if (queueBrowser == null) {
            queueBrowser = jmsContext.createBrowser(queue, msgSelector);
            queueBrowsers.put(queueBrowserCacheKey, queueBrowser);
        }

        return queueBrowser;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) { super.onActivityDefUpdate(activityDef); }
    public OpSequence<OpDispenser<S4JOp>> getSequencer() { return sequence; }

    public S4JConnInfo getS4JConnInfo() { return s4JConnInfo; }
    public JMSContext getJmsContext() { return jmsContext; }

    public Timer getBindTimer() { return bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Counter getBytesCounter() { return bytesCounter; }
    public Histogram getMessagesizeHistogram() { return messageSizeHistogram; }

    public NBErrorHandler getErrorhandler() { return errorhandler; }

    public void failOnAsyncOperationFailure() {
        if (asyncOperationFailure != null) {
            throw new RuntimeException(asyncOperationFailure);
        }
    }
    public void asyncOperationFailed(Throwable ex) {
        this.asyncOperationFailure = ex;
    }
}
