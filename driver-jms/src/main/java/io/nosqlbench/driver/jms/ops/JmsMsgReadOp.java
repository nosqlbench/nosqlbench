package io.nosqlbench.driver.jms.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.jms.JmsActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

public class JmsMsgReadOp extends JmsTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(JmsMsgReadOp.class);

    private final JmsActivity jmsActivity;
    private final boolean asyncJmsOp;
    private final Destination jmsDestination;

    private final JMSContext jmsContext;
    private final JMSConsumer jmsConsumer;
    private final boolean jmsConsumerDurable;
    private final boolean jmsConsumerShared;
    private final String jmsMsgSubscrption;
    private final String jmsMsgReadSelector;
    private final boolean jmsMsgNoLocal;
    private final long jmsReadTimeout;

    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;

    public JmsMsgReadOp(JmsActivity jmsActivity,
                        boolean asyncJmsOp,
                        Destination jmsDestination,
                        boolean jmsConsumerDurable,
                        boolean jmsConsumerShared,
                        String jmsMsgSubscrption,
                        String jmsMsgReadSelector,
                        boolean jmsMsgNoLocal,
                        long jmsReadTimeout) {
        this.jmsActivity = jmsActivity;
        this.asyncJmsOp = asyncJmsOp;
        this.jmsDestination = jmsDestination;
        this.jmsConsumerDurable = jmsConsumerDurable;
        this.jmsConsumerShared = jmsConsumerShared;
        this.jmsMsgReadSelector = jmsMsgReadSelector;
        this.jmsMsgSubscrption = jmsMsgSubscrption;
        this.jmsMsgNoLocal = jmsMsgNoLocal;
        this.jmsReadTimeout = jmsReadTimeout;

        this.jmsContext = jmsActivity.getJmsContext();
        this.jmsConsumer = createJmsConsumer();

        this.bytesCounter = jmsActivity.getBytesCounter();
        this.messagesizeHistogram = jmsActivity.getMessagesizeHistogram();
    }

    private JMSConsumer createJmsConsumer() {
        JMSConsumer jmsConsumer;

        try {
            if (jmsConsumerDurable) {
                if (jmsConsumerShared)
                    jmsConsumer = jmsContext.createSharedDurableConsumer((Topic) jmsDestination, jmsMsgSubscrption, jmsMsgReadSelector);
                else
                    jmsConsumer = jmsContext.createDurableConsumer((Topic) jmsDestination, jmsMsgSubscrption, jmsMsgReadSelector, jmsMsgNoLocal);
            } else {
                if (jmsConsumerShared)
                    jmsConsumer = jmsContext.createSharedConsumer((Topic) jmsDestination, jmsMsgSubscrption, jmsMsgReadSelector);
                else
                    jmsConsumer = jmsContext.createConsumer(jmsDestination, jmsMsgReadSelector, jmsMsgNoLocal);
            }
        }
        catch (InvalidDestinationRuntimeException invalidDestinationRuntimeException) {
            throw new RuntimeException("Failed to create JMS consumer: invalid destination!");
        }
        catch (InvalidSelectorRuntimeException invalidSelectorRuntimeException) {
            throw new RuntimeException("Failed to create JMS consumer: invalid message selector!");
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            jmsRuntimeException.printStackTrace();
            throw new RuntimeException("Failed to create JMS consumer: runtime internal error!");
        }

        // TODO: async consumer
//        if (this.asyncJmsOp) {
//            jmsConsumer.setMessageListener();
//        }

        return jmsConsumer;
    }

    @Override
    public void run() {
        // FIXME: jmsReadTimeout being 0 behaves like receiveNoWait() instead of waiting indefinitley
        Message receivedMsg = jmsConsumer.receive(jmsReadTimeout);
        try {
            if (receivedMsg != null) {
                receivedMsg.acknowledge();
                byte[] receivedMsgBody = receivedMsg.getBody(byte[].class);

                if (logger.isDebugEnabled()) {
                    logger.debug("received msg-payload={}", new String(receivedMsgBody));
                }

                int messagesize = receivedMsgBody.length;
                bytesCounter.inc(messagesize);
                messagesizeHistogram.update(messagesize);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to acknowledge the received JMS message.");
        }
    }
}
