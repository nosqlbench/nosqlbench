package io.nosqlbench.activitytypes.http;

import io.nosqlbench.activityapi.core.ActionDispenser;
import io.nosqlbench.activityapi.core.ActivityType;
import io.nosqlbench.activityimpl.ActivityDef;
import io.virtdata.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service(ActivityType.class)
public class HttpActivityType implements ActivityType<HttpActivity> {

    private static final Logger logger = LoggerFactory.getLogger(HttpActivityType.class);

    @Override
    public String getName() {
        return "http";
    }

    @Override
    public ActionDispenser getActionDispenser(HttpActivity activity) {
        return new HttpActionDispenser(activity);
    }

    @Override
    public HttpActivity getActivity(ActivityDef activityDef) {
        return new HttpActivity(activityDef);
    }
}
