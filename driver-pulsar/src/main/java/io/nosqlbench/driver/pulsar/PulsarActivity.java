package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.PulsarOp;
import io.nosqlbench.driver.pulsar.ops.ReadyPulsarOp;
import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PulsarActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(PulsarActivity.class);

    public Timer bindTimer;
    public Timer executeTimer;
    public Counter bytesCounter;
    public Histogram messagesizeHistogram;
    private PulsarSpaceCache pulsarCache;

    private PulsarNBClientConf clientConf;
    private String serviceUrl;

    private NBErrorHandler errorhandler;
    private OpSequence<OpDispenser<PulsarOp>> sequencer;

    // private Supplier<PulsarSpace> clientSupplier;
    // private ThreadLocal<Supplier<PulsarClient>> tlClientSupplier;

    public PulsarActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        bytesCounter = ActivityMetrics.counter(activityDef, "bytes");
        messagesizeHistogram = ActivityMetrics.histogram(activityDef, "messagesize");
        String pulsarClntConfFile = activityDef.getParams().getOptionalString("config").orElse("config.properties");
        clientConf = new PulsarNBClientConf(pulsarClntConfFile);

        serviceUrl = activityDef.getParams().getOptionalString("service_url").orElse("pulsar://localhost:6650");

        pulsarCache = new PulsarSpaceCache(this);

        this.sequencer = createOpSequence((ot) -> new ReadyPulsarOp(ot, pulsarCache, this));
        setDefaultsFromOpSequence(sequencer);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );
    }

    public NBErrorHandler getErrorhandler() {
        return errorhandler;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    public OpSequence<OpDispenser<PulsarOp>> getSequencer() {
        return sequencer;
    }

    public PulsarNBClientConf getPulsarConf() {
        return clientConf;
    }

    public String getPulsarServiceUrl() {
        return serviceUrl;
    }

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
}
