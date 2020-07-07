package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

public class SwitchToFrameIdx implements WebDriverVerb {
    private final int index;

    public SwitchToFrameIdx(int index) {
        this.index = index;
    }

    @Override
    public void execute(WebContext context) {
        WebDriver frame = context.driver().switchTo().frame(index);
        context.setFocus(frame);
    }
}
