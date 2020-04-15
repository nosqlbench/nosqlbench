package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class NavigateForward implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        context.driver().navigate().forward();
    }
}
