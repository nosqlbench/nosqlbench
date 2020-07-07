package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class IsElementSelected implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        boolean selected = context.peekElement().isSelected();
        context.setVar("element.selected",selected);
    }
}
