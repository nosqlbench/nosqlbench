package io.nosqlbench.activitytype.http;

import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.engine.api.templating.StrInterpolator;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;
import io.nosqlbench.virtdata.core.templates.StringBindings;
import io.nosqlbench.virtdata.core.templates.StringBindingsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class HttpActivity extends SimpleActivity implements Activity, ActivityDefObserver {
    private final static Logger logger = LoggerFactory.getLogger(HttpActivity.class);
    private final ActivityDef activityDef;

    public StmtsDocList getStmtsDocList() {
        return stmtsDocList;
    }

    private StmtsDocList stmtsDocList;


    private int stride;
    private Integer maxTries;
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

    private OpSequence<CommandTemplate> opSequence;

    public HttpActivity(ActivityDef activityDef) {
        super(activityDef);
        this.activityDef = activityDef;
    }



    @Override
    public void initActivity() {
        super.initActivity();

        stride = activityDef.getParams().getOptionalInteger("stride").orElse(1);

        maxTries = activityDef.getParams().getOptionalInteger("maxTries").orElse(1);
        showstmnts = activityDef.getParams().getOptionalBoolean("showstmnts").orElse(false);

        hosts = activityDef.getParams().getOptionalString("host").orElse("localhost").split(",");
        port = activityDef.getParams().getOptionalInteger("port").orElse(80);

        this.opSequence = createDefaultOpSequence();
        setDefaultsFromOpSequence(opSequence);

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

    public OpSequence<CommandTemplate> getOpSequence() {
        return opSequence;
    }
}
