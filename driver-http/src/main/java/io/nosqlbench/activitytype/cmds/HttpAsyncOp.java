package io.nosqlbench.activitytype.cmds;

import io.nosqlbench.activitytype.http.async.HttpAsyncAction;

import java.net.http.HttpClient;

public class HttpAsyncOp {
    public final HttpAsyncAction action;
    public final ReadyHttpOp op;
    public final long cycle;

    private final HttpOp httpOp;
    private final HttpClient client;

    public HttpAsyncOp(HttpAsyncAction action, ReadyHttpOp op, long cycle, HttpClient client) {
        this.action = action;
        this.op = op;
        this.cycle = cycle;
        this.client = client;
        this.httpOp = op.apply(cycle);

    }

    public HttpOp getOp() {
        return httpOp;
    }

    public HttpClient getClient() {
        return client;
    }

    public HttpAsyncAction getAction() {
        return action;
    }

}
