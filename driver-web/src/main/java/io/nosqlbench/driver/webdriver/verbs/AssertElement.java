package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import io.nosqlbench.nb.api.errors.BasicError;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssertElement implements WebDriverVerb {
    private final static Logger logger = LoggerFactory.getLogger(AssertElement.class);
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
