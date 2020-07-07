package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class WindowMaximize implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        context.driver().manage().window().maximize();
    }
}
