package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;

public class ClearElementBy implements WebDriverVerb {
    private final By by;

    public ClearElementBy(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        context.peekElement().findElement(by).clear();
    }
}
