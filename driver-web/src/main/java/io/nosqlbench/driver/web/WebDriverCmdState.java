package io.nosqlbench.driver.web;

public class WebDriverCmdState {

    private final WebDriverAsyncAction action;
    private final long cycle;

    public WebDriverCmdState(WebDriverAsyncAction action, long cycle) {

        this.action = action;
        this.cycle = cycle;
    }
}
