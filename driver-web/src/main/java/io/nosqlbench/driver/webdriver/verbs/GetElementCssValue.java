package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

class GetElementCssValue implements WebDriverVerb {

    private final String propname;
    public GetElementCssValue(String propname) {
        this.propname = propname;
    }
    @Override
    public void execute(WebContext context) {
        String cssValue = context.peekElement().getCssValue(propname);
        context.setVar("element.cssvalue",cssValue);
    }
}
