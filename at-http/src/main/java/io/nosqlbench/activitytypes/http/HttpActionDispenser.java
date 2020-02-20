package io.nosqlbench.activitytypes.http;

import io.nosqlbench.activityapi.core.Action;
import io.nosqlbench.activityapi.core.ActionDispenser;

public class HttpActionDispenser implements ActionDispenser {
    private HttpActivity httpActivity;

    public HttpActionDispenser(HttpActivity httpActivity) {
        this.httpActivity = httpActivity;
    }

    @Override
    public Action getAction(int i) {
        return new HttpAction(httpActivity.getActivityDef(), i, httpActivity);
    }
}