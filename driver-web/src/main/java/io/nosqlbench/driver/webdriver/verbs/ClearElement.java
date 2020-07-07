package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class ClearElement implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        context.peekElement().clear();
    }
}
