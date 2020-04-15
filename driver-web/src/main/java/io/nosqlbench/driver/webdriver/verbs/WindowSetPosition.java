package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Point;

public class WindowSetPosition implements WebDriverVerb {
    private final int x;
    private final int y;

    public WindowSetPosition(String x, String y) {
        this.x = Integer.parseInt(x);
        this.y = Integer.parseInt(y);
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().window().setPosition(new Point(x, y));
    }
}
