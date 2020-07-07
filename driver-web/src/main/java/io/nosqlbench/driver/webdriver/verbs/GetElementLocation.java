package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Point;

public class GetElementLocation implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Point location = context.peekElement().getLocation();
        context.setVar("element.location",location);
    }
}
