package io.nosqlbench.driver.jms;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.jms.conn.JmsConnInfo;
import io.nosqlbench.driver.jms.conn.JmsPulsarConnInfo;
import io.nosqlbench.driver.jms.ops.JmsOp;
import io.nosqlbench.driver.jms.util.JmsHeader;
import io.nosqlbench.driver.jms.util.JmsUtil;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JmsActivity extends SimpleActivity {

    private final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();

    private String jmsProviderType;
    private JmsConnInfo jmsConnInfo;

    private JMSContext jmsContext;

    private OpSequence<OpDispenser<JmsOp>> sequence;
    private volatile Throwable asyncOperationFailure;
    private NBErrorHandler errorhandler;

    private Timer bindTimer;
    private Timer executeTimer;
    private Counter bytesCounter;
    private Histogram messagesizeHistogram;

    public JmsActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();

        // default JMS type: Pulsar
        jmsProviderType =
            activityDef.getParams()
                .getOptionalString(JmsUtil.JMS_PROVIDER_TYPE_KEY_STR)
                .orElse(JmsUtil.JMS_PROVIDER_TYPES.PULSAR.label);

        if (StringUtils.equalsIgnoreCase(jmsProviderType, JmsUtil.JMS_PROVIDER_TYPES.PULSAR.label )) {
            jmsConnInfo = new JmsPulsarConnInfo(jmsProviderType, activityDef);
        }

        PulsarConnectionFactory factory;
        if (StringUtils.equalsIgnoreCase(jmsProviderType, JmsUtil.JMS_PROVIDER_TYPES.PULSAR.label )) {
            Map<String, Object> configuration = new HashMap<>();
            configuration.put("webServiceUrl", ((JmsPulsarConnInfo)jmsConnInfo).getWebSvcUrl());
            configuration.put("brokerServiceUrl",((JmsPulsarConnInfo)jmsConnInfo).getPulsarSvcUrl());

            try {
                factory = new PulsarConnectionFactory(configuration);
                this.jmsContext = factory.createContext();
            } catch (JMSException e) {
                throw new RuntimeException(
                    "Unable to initialize JMS connection factory (driver type: " + jmsProviderType + ")!");
            }
        }
        else {
            throw new RuntimeException("Unsupported JMS driver type : " + jmsProviderType);
        }

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messagesizeHistogram = ActivityMetrics.histogram(activityDef, "messagesize");

        if (StringUtils.equalsIgnoreCase(jmsProviderType, JmsUtil.JMS_PROVIDER_TYPES.PULSAR.label )) {
            this.sequence = createOpSequence((ot) -> new ReadyPulsarJmsOp(ot, this));
        }

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
    public Destination getOrCreateJmsDestination(String jmsDestinationType, String destName) {
        String encodedTopicStr =
            JmsUtil.encode(jmsDestinationType, destName);
        Destination destination = jmsDestinations.get(encodedTopicStr);

        if ( destination == null ) {
            // TODO: should we match Persistent/Non-peristent JMS Delivery mode with
            //       Pulsar Persistent/Non-prsistent topic?
            if (StringUtils.equalsIgnoreCase(jmsDestinationType, JmsUtil.JMS_DESTINATION_TYPES.QUEUE.label)) {
                destination = jmsContext.createQueue(destName);
            } else if (StringUtils.equalsIgnoreCase(jmsDestinationType, JmsUtil.JMS_DESTINATION_TYPES.TOPIC.label)) {
                destination = jmsContext.createTopic(destName);
            }

            jmsDestinations.put(encodedTopicStr, destination);
        }

        return destination;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) { super.onActivityDefUpdate(activityDef); }
    public OpSequence<OpDispenser<JmsOp>> getSequencer() { return sequence; }

    public String getJmsProviderType() { return jmsProviderType; }
    public JmsConnInfo getJmsConnInfo() { return jmsConnInfo; }
    public JMSContext getJmsContext() { return jmsContext; }

    public Timer getBindTimer() { return bindTimer; }
    public Timer getExecuteTimer() { return this.executeTimer; }
    public Counter getBytesCounter() { return bytesCounter; }
    public Histogram getMessagesizeHistogram() { return messagesizeHistogram; }

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
