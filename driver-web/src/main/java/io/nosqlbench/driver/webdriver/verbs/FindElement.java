package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindElement implements WebDriverVerb {
    private final static Logger logger = LoggerFactory.getLogger(FindElement.class);

    private final By by;

    public FindElement(String by) {
        this.by = Bys.get(by);
    }

    @Override
    public void execute(WebContext context) {
        try {
            WebElement element = context.driver().findElement(by);
            context.pushElement(element);
        } catch (NoSuchElementException nsee) {
            context.pushElement(null);
        }
    }

}
