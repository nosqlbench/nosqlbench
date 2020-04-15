package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Cookie;

public class DeleteCookie implements WebDriverVerb {

    private final Cookie cookie;

    public DeleteCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().deleteCookie(cookie);
    }
}
