/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.activitytype.http;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.cmds.HttpOp;
import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.SimpleActivity;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    public Histogram statusCodeHisto;

    private OpSequence<OpDispenser<? extends HttpOp>> sequencer;
    private boolean diagnosticsEnabled;
    private long timeout = Long.MAX_VALUE;
    private NBErrorHandler errorhandler;

    public HttpActivity(ActivityDef activityDef) {
        super(activityDef);
        this.activityDef = activityDef;
    }

    @Override
    public void initActivity() {
        super.initActivity();

        bindTimer = ActivityMetrics.timer(activityDef, "bind",this.getHdrDigits());
        executeTimer = ActivityMetrics.timer(activityDef, "execute", this.getHdrDigits());
        resultTimer = ActivityMetrics.timer(activityDef, "result", this.getHdrDigits());
        triesHisto = ActivityMetrics.histogram(activityDef, "tries", this.getHdrDigits());
        rowCounter = ActivityMetrics.meter(activityDef, "rows");
        statusCodeHisto = ActivityMetrics.histogram(activityDef, "statuscode",this.getHdrDigits());
        skippedTokens = ActivityMetrics.histogram(activityDef, "skipped-tokens",this.getHdrDigits());
        resultSuccessTimer = ActivityMetrics.timer(activityDef, "result-success", this.getHdrDigits());
        this.sequencer = createOpSequence(ReadyHttpOp::new, false);
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
            default:
                throw new RuntimeException("unable to recognize client scope: " + getClientScope());
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

    public OpSequence<OpDispenser<? extends HttpOp>> getSequencer() {
        return sequencer;
    }

    public boolean isDiagnosticMode() {
        return diagnosticsEnabled;
    }
}
