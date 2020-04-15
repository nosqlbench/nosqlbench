package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebElement;

public class SwitchToActiveElement implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        WebElement webElement = context.driver().switchTo().activeElement();
        context.pushElement(webElement);
    }
}
