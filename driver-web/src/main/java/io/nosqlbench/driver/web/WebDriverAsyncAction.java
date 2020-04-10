package io.nosqlbench.driver.web;

import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.function.LongFunction;

/**
 * Holds the definition and tracking state for a web driver command.
 */
public class WebDriverAsyncAction extends BaseAsyncAction<WebDriverCmdState,WebDriverActivity> {

    public WebDriverAsyncAction(WebDriverActivity activity, int slot) {
        super(activity,slot);
    }

    @Override
    public void startOpCycle(TrackedOp<WebDriverCmdState> opc) {
        WebDriver driver = new ChromeDriver();
        driver.get("http://docs.nosqlbench.io/");
    }

    @Override
    public LongFunction<WebDriverCmdState> getOpInitFunction() {
        return (cycle) -> new WebDriverCmdState(this,cycle);
    }
}
