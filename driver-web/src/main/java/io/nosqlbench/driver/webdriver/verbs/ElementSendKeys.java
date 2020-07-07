package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class ElementSendKeys implements WebDriverVerb {
    private final CharSequence keys;

    public ElementSendKeys(CharSequence keys) {
        this.keys = keys;
    }

    @Override
    public void execute(WebContext context) {
        context.peekElement().sendKeys(keys);
    }
}
