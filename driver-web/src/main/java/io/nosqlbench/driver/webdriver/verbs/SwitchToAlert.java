package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Alert;

public class SwitchToAlert implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Alert alert = context.driver().switchTo().alert();
        context.setAlert(alert);
    }
}
