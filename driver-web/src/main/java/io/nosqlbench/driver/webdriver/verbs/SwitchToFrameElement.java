package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.security.InvalidParameterException;

public class SwitchToFrameElement implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        WebElement webElement = context.peekElement();
        if (webElement==null) {
            throw new InvalidParameterException("There was no previously found element to switch to.");
        }
        WebDriver frame = context.driver().switchTo().frame(webElement);
        context.setFocus(frame);
    }
}
