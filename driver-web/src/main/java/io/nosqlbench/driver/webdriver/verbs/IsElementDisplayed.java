package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class IsElementDisplayed implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        boolean displayed = context.peekElement().isDisplayed();
        context.setVar("element.displayed",displayed);
    }
}
