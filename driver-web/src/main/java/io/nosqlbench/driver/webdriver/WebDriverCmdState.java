package io.nosqlbench.driver.webdriver;

public class WebDriverCmdState {

    private final WebDriverAction action;
    private final long cycle;

    public WebDriverCmdState(WebDriverAction action, long cycle) {

        this.action = action;
        this.cycle = cycle;
    }
}
