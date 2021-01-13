package io.nosqlbench.activitytype.cmds;

import java.net.http.HttpRequest;
import java.util.regex.Pattern;

public class HttpOp {
    public final Pattern ok_status;
    public final Pattern ok_body;
    public final HttpRequest request;

    public HttpOp(HttpRequest request, Pattern ok_status, Pattern ok_body) {
        this.request = request;
        this.ok_status = ok_status;
        this.ok_body = ok_body;
    }
}
