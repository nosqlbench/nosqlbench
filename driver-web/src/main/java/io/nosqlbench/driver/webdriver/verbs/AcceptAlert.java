package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Alert;

import java.security.InvalidParameterException;

public class AcceptAlert implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Alert alert = context.getAlert();
        if (alert==null) {
            alert = context.driver().switchTo().alert();
        }
        if (alert==null) {
            throw new InvalidParameterException("Alert is not present");
        }
        alert.accept();
        context.clearAlert();
    }
}
