package io.nosqlbench.driver.web;

import io.nosqlbench.engine.api.activityapi.core.Action;
import io.nosqlbench.engine.api.activityapi.core.ActionDispenser;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.annotations.Service;

@Service(ActivityType.class)
public class WebDriverActivityType implements ActivityType<WebDriverActivity> {

    @Override
    public String getName() {
        return "webdriver";
    }

    @Override
    public WebDriverActivity getActivity(ActivityDef activityDef) {
        return new WebDriverActivity(activityDef);
    }

    @Override
    public ActionDispenser getActionDispenser(WebDriverActivity activity) {
        return new WebDriverActionDispenser(activity);
    }

    private static class WebDriverActionDispenser implements ActionDispenser {

        private final Activity activity;

        private WebDriverActionDispenser(Activity activity) {
            this.activity = activity;
        }

        @Override
        public Action getAction(int slot) {
            return new WebDriverAsyncAction((WebDriverActivity) activity, slot);
        }
    }
}
