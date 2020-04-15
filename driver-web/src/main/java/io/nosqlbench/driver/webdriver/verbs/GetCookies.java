package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Cookie;

import java.util.Set;

public class GetCookies implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        Set<Cookie> cookies = context.driver().manage().getCookies();
        context.setCookies(cookies);
    }
}
