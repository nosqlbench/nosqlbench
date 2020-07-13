package io.nosqlbench.activitytype.cmds;

import java.net.http.HttpRequest;

public class HttpOp {
    public final String ok_status;
    public final String ok_body;
    public final HttpRequest request;

    public HttpOp(HttpRequest request, String ok_status, String ok_body) {
        this.request = request;
        this.ok_status = ok_status;
        this.ok_body = ok_body;
    }
}
