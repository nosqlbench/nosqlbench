package io.nosqlbench.driver.pularjms;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.pularjms.ops.PulsarJmsOp;
import io.nosqlbench.driver.pularjms.util.PulsarJmsActivityUtil;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PulsarJmsActivity extends SimpleActivity {

    private final ConcurrentHashMap<String, Destination> jmsDestinations = new ConcurrentHashMap<>();

    // e.g. pulsar://localhost:6650
    private String pulsarSvcUrl;
    // e.g. http://localhost:8080
    private String webSvcUrl;

    private JMSContext jmsContext;

    private OpSequence<OpDispenser<PulsarJmsOp>> sequence;
    private volatile Throwable asyncOperationFailure;
    private NBErrorHandler errorhandler;

    private Timer bindTimer;
    private Timer executeTimer;
    private Counter bytesCounter;
    private Histogram messagesizeHistogram;

    public PulsarJmsActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();

        webSvcUrl =
            activityDef.getParams().getOptionalString("web_url").orElse("http://localhost:8080");
        pulsarSvcUrl =
            activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("webServiceUrl", webSvcUrl);
        configuration.put("brokerServiceUrl", pulsarSvcUrl);

        PulsarConnectionFactory factory;
        try {
            factory = new PulsarConnectionFactory(configuration);
            this.jmsContext = factory.createContext();
        } catch (JMSException e) {
            throw new RuntimeException("PulsarJMS message send:: Unable to initialize Pulsar connection factory!");
        }

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messagesizeHistogram = ActivityMetrics.histogram(activityDef, "messagesize");

        this.sequence = createOpSequence((ot) -> new ReadyPulsarJmsOp(ot, this));
        setDefaultsFromOpSequence(sequence);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );
    }

    /**
     * If the JMS destination that corresponds to a topic exists, reuse it; Otherwise, create it
     *
     * @param pulsarTopic
     */
    public Destination getOrCreateJmsDestination(String pulsarTopic) {
        String encodedTopicStr = PulsarJmsActivityUtil.encode(pulsarTopic);
        Destination destination = jmsDestinations.get(encodedTopicStr);

        if ( destination == null ) {
            destination = jmsContext.createQueue(pulsarTopic);
            jmsDestinations.put(encodedTopicStr, destination);
        }

        return destination;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) { super.onActivityDefUpdate(activityDef); }
    public OpSequence<OpDispenser<PulsarJmsOp>> getSequencer() { return sequence; }

    public String getPulsarSvcUrl() {
        return pulsarSvcUrl;
    }
    public String getWebSvcUrl() { return webSvcUrl; }
    public JMSContext getJmsContext() { return jmsContext; }

    public Timer getBindTimer() {
        return bindTimer;
    }
    public Timer getExecuteTimer() {
        return this.executeTimer;
    }
    public Counter getBytesCounter() {
        return bytesCounter;
    }
    public Histogram getMessagesizeHistogram() {
        return messagesizeHistogram;
    }

    public NBErrorHandler getErrorhandler() {
        return errorhandler;
    }

    public void failOnAsyncOperationFailure() {
        if (asyncOperationFailure != null) {
            throw new RuntimeException(asyncOperationFailure);
        }
    }
    public void asyncOperationFailed(Throwable ex) {
        this.asyncOperationFailure = asyncOperationFailure;
    }
}
