package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.Cookie;

public class GetCookieNamed implements WebDriverVerb {
    private final String cookieName;

    public GetCookieNamed(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void execute(WebContext context) {
        Cookie cookie = context.driver().manage().getCookieNamed(cookieName);
        context.pushCookie(cookie);
    }
}
