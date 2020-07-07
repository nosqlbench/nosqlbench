package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

public class SwitchToFrameByName implements WebDriverVerb {
    private String name;

    public SwitchToFrameByName(String name) {
        this.name = name;
    }

    @Override
    public void execute(WebContext context) {
        WebDriver frame = context.driver().switchTo().frame(this.name);
        context.setFocus(frame);
    }
}
