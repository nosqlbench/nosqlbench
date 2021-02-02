package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.ReadyPulsarOp;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.function.Supplier;

public class PulsarActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(PulsarAction.class);

    public Timer bindTimer;
    public Timer executeTimer;
    private PulsarSpaceCache pulsarCache;

    private NBErrorHandler errorhandler;
    private String pulsarUrl;
    private OpSequence<ReadyPulsarOp> sequencer;
    private PulsarClient activityClient;

    private Supplier<PulsarSpace> clientSupplier;
//    private ThreadLocal<Supplier<PulsarClient>> tlClientSupplier;

    public PulsarActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");

        pulsarUrl = activityDef.getParams().getOptionalString("url").orElse("pulsar://localhost:6650");
        pulsarCache = new PulsarSpaceCache(this, this::newClient);
        this.sequencer = createOpSequence((ot) -> new ReadyPulsarOp(ot, pulsarCache));
        setDefaultsFromOpSequence(sequencer);
        onActivityDefUpdate(activityDef);

        this.errorhandler = new NBErrorHandler(
            () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
            this::getExceptionMetrics
        );
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    public PulsarClient newClient() {
        try {
            PulsarClient newClient = PulsarClient.builder().
                serviceUrl(this.pulsarUrl)
                .build();
            return newClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OpSequence<ReadyPulsarOp> getSequencer() {
        return sequencer;
    }

    public Timer getBindTimer() {
        return bindTimer;
    }

    public Timer getExecuteTimer() {
        return this.executeTimer;
    }
}
