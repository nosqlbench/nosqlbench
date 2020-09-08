package io.nosqlbench.activitytype.http;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.cmds.ReadyHttpRequest;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpActivity extends SimpleActivity implements Activity, ActivityDefObserver {
    private final static Logger logger = LoggerFactory.getLogger(HttpActivity.class);
    private final ActivityDef activityDef;

    public StmtsDocList getStmtsDocList() {
        return stmtsDocList;
    }

    private StmtsDocList stmtsDocList;


    private int stride;
    private Integer maxTries;
    private long timeout_ms = 30_000L;
    private Boolean showstmnts;
    public Timer bindTimer;
    public Timer executeTimer;
    public Histogram triesHisto;
    public Timer resultTimer;
    public Meter rowCounter;
    public Histogram skippedTokens;
    public Timer resultSuccessTimer;

    private String[] hosts;
    private int port;

    private OpSequence<ReadyHttpRequest> opSequence;

    public HttpActivity(ActivityDef activityDef) {
        super(activityDef);
        this.activityDef = activityDef;
    }



    @Override
    public void initActivity() {
        super.initActivity();

//        stride = activityDef.getParams().getOptionalInteger("stride").orElse(1);
        maxTries = activityDef.getParams().getOptionalInteger("maxTries").orElse(1);
        timeout_ms = activityDef.getParams().getOptionalLong("timeout_ms").orElse(30_000L);
//        showstmnts = activityDef.getParams().getOptionalBoolean("showstmnts").orElse(false);

//        hosts = activityDef.getParams().getOptionalString("host").orElse("localhost").split(",");
//        port = activityDef.getParams().getOptionalInteger("port").orElse(80);

        this.opSequence = createOpSequence(ReadyHttpRequest::new);
        this.setDefaultsFromOpSequence(opSequence);

        bindTimer = ActivityMetrics.timer(activityDef, "bind");
        executeTimer = ActivityMetrics.timer(activityDef, "execute");
        resultTimer = ActivityMetrics.timer(activityDef, "result");
        triesHisto = ActivityMetrics.histogram(activityDef, "tries");
        rowCounter = ActivityMetrics.meter(activityDef, "rows");
        skippedTokens = ActivityMetrics.histogram(activityDef, "skipped-tokens");
        resultSuccessTimer = ActivityMetrics.timer(activityDef,"result-success");

        onActivityDefUpdate(activityDef);
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        super.onActivityDefUpdate(activityDef);
    }

    public Integer getMaxTries() {
        return maxTries;
    }

    public Boolean getShowstmts() {
        return showstmnts;
    }

    public String[] getHosts() {
        return hosts;
    }

    public int getPort() {
        return port;
    }

    public OpSequence<ReadyHttpRequest> getOpSequence() {
        return opSequence;
    }

    public long getTimeoutMs() {
        return timeout_ms;
    }
}
