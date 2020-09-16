package io.nosqlbench.activitytype.http.async;

import io.nosqlbench.activitytype.cmds.HttpAsyncOp;
import io.nosqlbench.activitytype.cmds.HttpOp;
import io.nosqlbench.activitytype.cmds.ReadyHttpOp;
import io.nosqlbench.activitytype.http.HttpActivity;
import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongFunction;

public class HttpAsyncAction extends BaseAsyncAction<HttpAsyncOp, HttpActivity> {

    private final static Logger logger = LoggerFactory.getLogger(HttpAsyncAction.class);

    private OpSequence<ReadyHttpOp> sequencer;
    private HttpClient client;

    private CompletableFuture<HttpResponse<Void>> future;


    public HttpAsyncAction(HttpActivity httpActivity, int slot) {
        super(httpActivity,slot);
    }

    @Override
    public void startOpCycle(TrackedOp<HttpAsyncOp> opc) {
        HttpAsyncOp opdata = opc.getOpData();
        HttpOp op = opdata.getOp();

        opc.start();
        future = opdata.getAction().client.sendAsync(op.request, HttpResponse.BodyHandlers.discarding());
    }

    public void init() {
        this.sequencer = activity.getSequencer();
        this.client = activity.getClient().apply(Thread.currentThread());
    }

    @Override
    public LongFunction<HttpAsyncOp> getOpInitFunction() {
        return l -> {
            ReadyHttpOp readyHttpOp = sequencer.get(l);
            return new HttpAsyncOp(this,readyHttpOp,l,client);
        };
    }


//    @Override
//    public boolean enqueue(TrackedOp<HttpAsyncOp> opc) {
//        HttpAsyncOp opData = opc.getOpData();
//        opData.op.
//        return false;
//    }
}
