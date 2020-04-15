package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

public class SwitchToDefaultContent implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        WebDriver driver = context.driver().switchTo().defaultContent();
        context.setFocus(driver);
    }
}
