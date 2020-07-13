package io.nosqlbench.activitytype.http.async;

import io.nosqlbench.activitytype.cmds.HttpAsyncOp;
import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import io.nosqlbench.activitytype.http.HttpActivity;
import io.nosqlbench.engine.api.activityapi.core.AsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.function.LongFunction;

public class HttpAsyncAction implements AsyncAction<HttpAsyncOp> {

    private final static Logger logger = LoggerFactory.getLogger(HttpAsyncAction.class);

    private final HttpActivity httpActivity;
    private final int slot;

    private OpSequence<ReadyHttpOp> sequencer;
    private HttpClient client;

    public HttpAsyncAction(HttpActivity httpActivity, int slot) {
        this.httpActivity = httpActivity;
        this.slot = slot;
    }

    public void init() {
        this.sequencer = httpActivity.getSequencer();
        this.client = httpActivity.getClient().apply(Thread.currentThread());
    }

    @Override
    public LongFunction<HttpAsyncOp> getOpInitFunction() {
        return l -> {
            ReadyHttpOp readyHttpOp = sequencer.get(l);
            return new HttpAsyncOp(this,readyHttpOp,l);
        };
    }

    @Override
    public boolean enqueue(TrackedOp<HttpAsyncOp> opc) {
        HttpAsyncOp opData = opc.getOpData();
        opData.op.
        return false;
    }
}
