package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public class FindElementsInElementBy implements WebDriverVerb {

    private final By by;

    public FindElementsInElementBy(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        List<WebElement> elements = context.peekElement().findElements(by);
        context.setElements(elements);
    }
}
