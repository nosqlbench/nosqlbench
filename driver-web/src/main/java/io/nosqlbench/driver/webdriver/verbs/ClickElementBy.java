package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;

public class ClickElementBy implements WebDriverVerb {

    private final By by;

    public ClickElementBy(By by){
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().findElement(by).click();
    }
}
