package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class GetElementAttribute implements WebDriverVerb {
    private final String name;

    public GetElementAttribute(String name) {
        this.name = name;
    }

    @Override
    public void execute(WebContext context) {
        String attribute = context.peekElement().getAttribute(name);
        context.setVar("element.attribute",attribute);
    }

}
