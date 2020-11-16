package io.nosqlbench.activitytype.http;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.http.HttpClient;
import java.util.function.Function;

public class HttpActivity extends SimpleActivity implements Activity, ActivityDefObserver {
    private final static Logger logger = LogManager.getLogger(HttpActivity.class);
    private final ActivityDef activityDef;
    public HttpConsoleFormats console;

    // Used when sclientScope == ClientScope.activity
    private HttpClient activityClient;
    private ClientScope clientScope = ClientScope.activity;

    public Timer bindTimer;
    public Timer executeTimer;
    public Histogram triesHisto;
    public Timer resultTimer;
    public Meter rowCounter;
    public Histogram skippedTokens;
    public Timer resultSuccessTimer;

    private OpSequence<ReadyHttpOp> sequencer;
    private boolean diagnosticsEnabled;
    private long timeout = Long.MAX_VALUE;

    public HttpActivity(ActivityDef activityDef) {
        super(activityDef);
        this.activityDef = activityDef;
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");
        rowCounter = ActivityMetrics.meter(activityDef, "rows");
        skippedTokens = ActivityMetrics.histogram(activityDef, "skipped-tokens");
        resultSuccessTimer = ActivityMetrics.timer(activityDef, "result-success");
        this.sequencer = createOpSequence(ReadyHttpOp::new);
        setDefaultsFromOpSequence(sequencer);
        onActivityDefUpdate(activityDef);
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);

        this.console = getParams().getOptionalString("diag")
                .map(s -> HttpConsoleFormats.apply(s, this.console))
                .orElseGet(() -> HttpConsoleFormats.apply(null, null));

        this.diagnosticsEnabled = console.isDiagnosticMode();

        this.timeout = getParams().getOptionalLong("timeout").orElse(Long.MAX_VALUE);

        getParams().getOptionalString("client_scope")
                .map(ClientScope::valueOf)
                .ifPresent(this::setClientScope);

    }

    public long getTimeoutMillis() {
        return timeout;
    }

    private void setClientScope(ClientScope clientScope) {
        this.clientScope = clientScope;
    }

    public ClientScope getClientScope() {
        return clientScope;
    }

    public synchronized Function<Thread, HttpClient> getClient() {
        switch (getClientScope()) {
            case thread:
                return t -> newClient();
            case activity:
                if (this.activityClient == null) {
                    this.activityClient = newClient();
                }
                return t -> this.activityClient;
            default: throw new RuntimeException("unable to recoginize client scope: " + getClientScope());
        }
    }

    public HttpClient newClient() {
        HttpClient.Builder builder = HttpClient.newBuilder();
        HttpClient.Redirect follow_redirects = getParams().getOptionalString("follow_redirects")
                .map(String::toUpperCase)
                .map(HttpClient.Redirect::valueOf)
                .map(r -> {
                    logger.debug("follow_redirects=>" + r);
                    return r;
                }).orElse(HttpClient.Redirect.NORMAL);
        builder = builder.followRedirects(follow_redirects);
        return builder.build();
    }

    public OpSequence<ReadyHttpOp> getSequencer() {
        return sequencer;
    }

    public boolean isDiagnosticMode() {
        return diagnosticsEnabled;
    }
}
