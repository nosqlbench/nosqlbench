package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Point;

public class WindowGetPosition implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Point position = context.driver().manage().window().getPosition();
        context.setVar("window.position", position);
    }
}
