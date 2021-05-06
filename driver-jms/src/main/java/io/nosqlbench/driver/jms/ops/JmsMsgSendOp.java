package io.nosqlbench.driver.jms.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.jms.JmsActivity;
import io.nosqlbench.driver.jms.util.JmsHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class JmsMsgSendOp extends JmsTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(JmsMsgSendOp.class);

    private final JmsActivity jmsActivity;
    private final boolean asyncJmsOp;
    private final Destination jmsDestination;
    private final JmsHeader jmsHeader;
    private final Map<String, Object> jmsMsgProperties;

    private final JMSContext jmsContext;
    private final JMSProducer jmsProducer;
    private final String msgBody;

    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;

    public JmsMsgSendOp(JmsActivity jmsActivity,
                        boolean asyncJmsOp,
                        Destination jmsDestination,
                        JmsHeader jmsHeader,
                        Map<String, Object> jmsMsgProperties,
                        String msgBody) {
        this.jmsActivity = jmsActivity;
        this.asyncJmsOp = asyncJmsOp;
        this.jmsDestination = jmsDestination;

        this.jmsHeader = jmsHeader;
        this.jmsMsgProperties = jmsMsgProperties;
        this.msgBody = msgBody;

        if (!jmsHeader.isValidHeader()) {
            throw new RuntimeException(jmsHeader.getInvalidJmsHeaderMsgText());
        }

        if ((msgBody == null) || msgBody.isEmpty()) {
            throw new RuntimeException("JMS message body can't be empty!");
        }

        this.jmsContext = jmsActivity.getJmsContext();
        this.jmsProducer = createJmsProducer();

        this.bytesCounter = jmsActivity.getBytesCounter();
        this.messagesizeHistogram = jmsActivity.getMessagesizeHistogram();
    }

    private JMSProducer createJmsProducer() {
        JMSProducer jmsProducer = this.jmsContext.createProducer();

        jmsProducer.setDeliveryMode(this.jmsHeader.getDeliveryMode());
        jmsProducer.setPriority(this.jmsHeader.getMsgPriority());
        jmsProducer.setDeliveryDelay(this.jmsHeader.getMsgDeliveryDelay());
        jmsProducer.setDisableMessageTimestamp(this.jmsHeader.isDisableMsgTimestamp());
        jmsProducer.setDisableMessageID(this.jmsHeader.isDisableMsgId());

        if (this.asyncJmsOp) {
            jmsProducer.setAsync(new CompletionListener() {
                @Override
                public void onCompletion(Message msg) {
                    try {
                        byte[] msgBody = msg.getBody(byte[].class);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Async message send success - message body: " + new String(msgBody));
                        }
                    }
                    catch (JMSException jmsException) {
                        jmsException.printStackTrace();
                        logger.warn("Unexpected error when parsing message body: " + jmsException.getMessage());
                    }
                }

                @Override
                public void onException(Message msg, Exception e) {
                    try {
                        byte[] msgBody = msg.getBody(byte[].class);
                        if (logger.isTraceEnabled()) {
                            logger.trace("Async message send failure - message body: " + new String(msgBody));
                        }
                    }
                    catch (JMSException jmsException) {
                        jmsException.printStackTrace();
                        logger.warn("Unexpected error when parsing message body: " + jmsException.getMessage());
                    }
                }
            });
        }

        for (Map.Entry<String, Object> entry : jmsMsgProperties.entrySet()) {
            jmsProducer.setProperty(entry.getKey(), entry.getValue());
        }

        return jmsProducer;
    }

    @Override
    public void run() {
        int messageSize;
        try {
            byte[] msgBytes = msgBody.getBytes(StandardCharsets.UTF_8);
            messageSize = msgBytes.length;
            jmsProducer.send(jmsDestination, msgBody.getBytes(StandardCharsets.UTF_8));

            messagesizeHistogram.update(messageSize);
            bytesCounter.inc(messageSize);
        }
        catch (Exception ex) {
            logger.error("Failed to send JMS message - " + msgBody);
        }
    }
}
