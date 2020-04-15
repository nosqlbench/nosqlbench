package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class DeleteCookieNamed implements WebDriverVerb {

    private final String cookieName;

    public DeleteCookieNamed(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().deleteCookieNamed(cookieName);
    }
}
