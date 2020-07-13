package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.activitytype.http.async.HttpAsyncAction;

import java.net.http.HttpRequest;

public class HttpAsyncOp {
    public final HttpAsyncAction action;
    public final ReadyHttpOp op;
    public final long cycle;

    public HttpAsyncOp(HttpAsyncAction action, ReadyHttpOp op, long cycle) {
        this.action = action;
        this.op = op;
        this.cycle = cycle;
    }

    public ReadyHttpOp getOp() {
        return op;
    }

    public HttpRequest getRequest() {
        HttpOp httpOp = op.apply(cycle);
    }

}
