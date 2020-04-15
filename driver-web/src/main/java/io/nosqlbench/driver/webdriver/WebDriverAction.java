package io.nosqlbench.driver.webdriver;

import io.nosqlbench.driver.webdriver.verbs.WebDriverVerbs;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.openqa.selenium.WebDriver;

/**
 * Holds the definition and tracking state for a web driver command.
 */
public class WebDriverAction implements SyncAction, ActivityDefObserver {

    private final WebDriverActivity activity;
    private final int slot;
    // warn or count, or stop
    private String errors;
    private boolean dryrun;
    private WebContext context;

    public WebDriverAction(WebDriverActivity activity, int slot) {
        super();
        this.activity = activity;
        this.slot = slot;
    }

    @Override
    public void init() {
        onActivityDefUpdate(activity.getActivityDef());
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        this.errors = activityDef.getParams().getOptionalString("errors").orElse("stop");
        this.dryrun = activityDef.getParams().getOptionalBoolean("dryrun").orElse(false);
        this.context = activity.getWebContext(slot);
    }

    // TODO: resolve inner templates early, perhaps optionally
    // As it is right now, all commands are resolved dynamically, which is still not going to be the limiting
    // factor.
    @Override
    public int runCycle(long value) {
        try {
            CommandTemplate commandTemplate = activity.getOpSequence().get(value);
            WebDriverVerbs.execute(value, commandTemplate, context, dryrun);
            return 0;
        } catch (Exception e) {
            return 1;
        }

    }

}
