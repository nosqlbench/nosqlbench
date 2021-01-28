package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.ReadyPulsarOp;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.scoping.ScopedSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.function.Supplier;

public class PulsarActivity extends SimpleActivity implements ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(PulsarAction.class);

    public Timer bindTimer;
    public Timer executeTimer;

    public enum PulsarClientScope {
        activity,
        thread
    }

    private NBErrorHandler errorhandler;
    private String pulsarUrl;
    private OpSequence<ReadyPulsarOp> sequencer;
    private PulsarClientScope clientScope = PulsarClientScope.activity;
    private PulsarClient activityClient;

    private Supplier<PulsarClient> clientSupplier;
    private ThreadLocal<Supplier<PulsarClient>> tlClientSupplier;

    public PulsarActivity(ActivityDef activityDef) {
        super(activityDef);
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");

        pulsarUrl = activityDef.getParams().getOptionalString("url").orElse("pulsar://localhost:6650");

        ScopedSupplier clientScope = ScopedSupplier.valueOf(getParams().getOptionalString("client_scope").orElse("singleton"));
        this.clientSupplier = clientScope.supplier(this::newClient);
        PulsarClient pulsarClient = this.clientSupplier.get();
        this.sequencer = createOpSequence((ot) -> new ReadyPulsarOp(ot, this.clientSupplier));

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
        this.clientScope = PulsarClientScope.valueOf(activityDef.getParams().getOptionalString("scope").orElse("activity"));
    }

//    public synchronized Function<Thread, PulsarClient> getClient() {
//        switch (getClientScope()) {
//            case thread:
//                return t -> newClient();
//            case activity:
//                if (this.activityClient == null) {
//                    this.activityClient = newClient();
//                }
//                return t -> this.activityClient;
//            default:
//                throw new RuntimeException("unable to recognize client scope: " + getClientScope());
//        }
//
//    }

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

    private PulsarClientScope getClientScope() {
        return clientScope;
    }

    public OpSequence<ReadyPulsarOp> getSequencer() {
        return sequencer;
    }

    public Timer getBindTimer() {
        return bindTimer;
    }
}
