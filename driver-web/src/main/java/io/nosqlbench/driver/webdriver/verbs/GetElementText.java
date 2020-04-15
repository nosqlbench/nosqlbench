package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class GetElementText implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        String text = context.peekElement().getText();
        context.setVar("element.text",text);
    }
}
