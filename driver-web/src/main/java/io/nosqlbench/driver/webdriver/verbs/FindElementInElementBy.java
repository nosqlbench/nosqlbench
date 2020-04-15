package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FindElementInElementBy implements WebDriverVerb {
    private final By by;

    public FindElementInElementBy(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        WebElement element = context.peekElement().findElement(by);
        context.pushElement(element);
    }
}
