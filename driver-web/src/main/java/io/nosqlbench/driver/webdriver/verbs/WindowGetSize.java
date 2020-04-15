package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Dimension;

public class WindowGetSize implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Dimension size = context.driver().manage().window().getSize();
        context.setVar("window.size",size);
    }
}
