package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

public class Open implements WebDriverVerb {
    private final String url;
    private final String target;

    public Open(String url, String target) {
        this.url = url;
        this.target = target;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().get(url+target);
    }
}
