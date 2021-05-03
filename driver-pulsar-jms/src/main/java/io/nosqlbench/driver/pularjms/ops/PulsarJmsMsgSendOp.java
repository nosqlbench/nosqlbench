package io.nosqlbench.driver.pularjms.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.pularjms.PulsarJmsActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import java.nio.charset.StandardCharsets;

public class PulsarJmsMsgSendOp extends PulsarJmsTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(PulsarJmsMsgSendOp.class);

    private final PulsarJmsActivity pulsarActivity;
    private final boolean asyncPulsarOp;
    private final Destination jmsDestination;
    private final JMSContext jmsContext;
    private final JMSProducer jmsProducer;
    private final String msgBody;

    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;

    public PulsarJmsMsgSendOp(PulsarJmsActivity pulsarActivity,
                              boolean asyncPulsarOp,
                              Destination jmsDestination,
                              String msgBody) {
        this.pulsarActivity = pulsarActivity;
        this.asyncPulsarOp = asyncPulsarOp;
        this.jmsDestination = jmsDestination;
        this.jmsContext = pulsarActivity.getJmsContext();
        this.jmsProducer = jmsContext.createProducer();
        this.msgBody = msgBody;
        this.bytesCounter = pulsarActivity.getBytesCounter();
        this.messagesizeHistogram = pulsarActivity.getMessagesizeHistogram();
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
