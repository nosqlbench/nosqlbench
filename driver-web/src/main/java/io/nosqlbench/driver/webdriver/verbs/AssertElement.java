package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AssertElement implements WebDriverVerb {
    private final static Logger logger = LogManager.getLogger(AssertElement.class);
    private final By by;

    public AssertElement(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        WebElement element = context.driver().findElement(by);
        context.pushElement(element);
    }

}
