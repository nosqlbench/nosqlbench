package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

public class SwitchToWindow implements WebDriverVerb {
    private final String windowName;

    public SwitchToWindow(String windowName) {
        this.windowName = windowName;
    }

    @Override
    public void execute(WebContext context) {
        WebDriver window = context.driver().switchTo().window(this.windowName);
        context.setFocus(window);
    }
}
