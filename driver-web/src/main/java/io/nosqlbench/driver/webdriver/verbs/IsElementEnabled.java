package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class IsElementEnabled implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        boolean enabled = context.peekElement().isEnabled();
        context.setVar("element.enabled",enabled);
    }
}
