package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Cookie;

public class AddCookie implements WebDriverVerb {

    private final Cookie cookie;

    public AddCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().addCookie(this.cookie);
    }
}
