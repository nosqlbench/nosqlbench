package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class GetElementTagname implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        String tagName = context.peekElement().getTagName();
        context.setVar("element.tagname",tagName);
    }
}
