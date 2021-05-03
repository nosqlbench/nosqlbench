package io.nosqlbench.driver.jms.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.jms.JmsActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import java.nio.charset.StandardCharsets;

public class JmsMsgSendOp extends JmsTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(JmsMsgSendOp.class);

    private final JmsActivity jmsActivity;
    private final boolean asyncJmsOp;
    private final Destination jmsDestination;
    private final JMSContext jmsContext;
    private final JMSProducer jmsProducer;
    private final String msgBody;

    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;

    public JmsMsgSendOp(JmsActivity jmsActivity,
                        boolean asyncJmsOp,
                        Destination jmsDestination,
                        String msgBody) {
        this.jmsActivity = jmsActivity;
        this.asyncJmsOp = asyncJmsOp;
        this.jmsDestination = jmsDestination;
        this.jmsContext = jmsActivity.getJmsContext();
        this.jmsProducer = jmsContext.createProducer();
        this.msgBody = msgBody;
        this.bytesCounter = jmsActivity.getBytesCounter();
        this.messagesizeHistogram = jmsActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {
        if ((msgBody == null) || msgBody.isEmpty()) {
            throw new RuntimeException("JMS message body can't be empty!");
        }

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
