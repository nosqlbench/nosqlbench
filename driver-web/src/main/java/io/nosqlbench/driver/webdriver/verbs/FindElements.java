package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;

public class FindElements implements WebDriverVerb {
    private final static Logger logger = LogManager.getLogger(FindElements.class);

    private final By by;

    public FindElements(String by) {
        this.by = Bys.get(by);
    }

    @Override
    public void execute(WebContext context) {
        try {
            List<WebElement> elements = context.driver().findElements(by);
            context.setElements(elements);
        } catch (NoSuchElementException nsee) {
            context.pushElement(null);
        }
    }

}
