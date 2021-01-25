package io.nosqlbench.driver.webdriver;

import io.nosqlbench.driver.webdriver.verbs.WebDriverVerbs;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the definition and tracking state for a web driver command.
 */
public class WebDriverAction implements SyncAction, ActivityDefObserver {
    private final static Logger logger = LogManager.getLogger(WebDriverAction.class);

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
    public int runCycle(long cycle) {

        CommandTemplate commandTemplate = activity.getOpSequence().get(cycle);
        try {
            WebDriverVerbs.execute(cycle, commandTemplate, context, dryrun);
            return 0;

        } catch (Exception e) {
            logger.error("Error with cycle(" + cycle + "), statement(" + commandTemplate.getName() + "): " + e.getMessage());
            if (errors.equals("stop")) {
                throw e;
            }
            return 1;
        }

    }

}
