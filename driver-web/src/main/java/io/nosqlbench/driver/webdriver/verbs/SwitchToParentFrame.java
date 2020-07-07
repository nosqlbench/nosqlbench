package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

public class SwitchToParentFrame implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        WebDriver webDriver = context.driver().switchTo().parentFrame();
        context.setFocus(webDriver);
    }
}
