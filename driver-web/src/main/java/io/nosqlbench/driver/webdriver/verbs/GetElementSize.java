package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Dimension;

public class GetElementSize implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Dimension size = context.peekElement().getSize();
        context.setVar("element.size",size);
    }
}
