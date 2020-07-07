package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SwitchToFrameElementBy implements WebDriverVerb {

    private final By by;

    public SwitchToFrameElementBy(String by) {
        this.by = Bys.get(by);
    }

    @Override
    public void execute(WebContext context) {
        WebElement element = context.driver().findElement(by);
        WebDriver frame = context.driver().switchTo().frame(element);
        context.setFocus(frame);
    }
}
