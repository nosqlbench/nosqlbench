package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.OutputType;

public class ElementScreenShot implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        String screenshotAs = context.peekElement().getScreenshotAs(OutputType.BASE64);
        // This needs more work
    }
}
