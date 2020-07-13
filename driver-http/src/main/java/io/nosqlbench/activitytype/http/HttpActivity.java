package io.nosqlbench.activitytype.http;

import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class HttpActivity extends SimpleActivity implements Activity, ActivityDefObserver {
    private final static Logger logger = LoggerFactory.getLogger(HttpActivity.class);
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

        String[] diag = getParams().getOptionalString("diag").orElse("").split(",");
        Set<String> diags = new HashSet<String>(Arrays.asList(diag));

        this.console = new HttpConsoleFormats(diags);

        getParams().getOptionalString("client_scope").map(ClientScope::valueOf).ifPresent(this::setClientScope);

        this.sequencer = createOpSequence(ReadyHttpOp::new);
        setDefaultsFromOpSequence(sequencer);
        onActivityDefUpdate(activityDef);
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
        getParams().getOptionalString("follow_redirects")
                .map(String::toUpperCase)
                .map(HttpClient.Redirect::valueOf)
                .map(r -> {
                    logger.debug("follow_redirects=>" + r);
                    return r;
                })
                .ifPresent(builder::followRedirects);

        return builder.build();
    }

    public OpSequence<ReadyHttpOp> getSequencer() {
        return sequencer;
    }
}
