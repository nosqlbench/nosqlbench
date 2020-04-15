package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Dimension;

public class WindowSetSize implements WebDriverVerb {
    private final int width;
    private final int height;

    public WindowSetSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public WindowSetSize(String windowSize) {
        String[] xes = windowSize.split("x");
        this.width = Integer.parseInt(xes[0]);
        this.height = Integer.parseInt(xes[1]);
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().window().setSize(new Dimension(width, height));
    }
}
