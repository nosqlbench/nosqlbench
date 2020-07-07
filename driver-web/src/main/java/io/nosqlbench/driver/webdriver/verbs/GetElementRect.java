package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Rectangle;

public class GetElementRect implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Rectangle rect = context.peekElement().getRect();
        context.setVar("element.rect",rect);
    }
}
