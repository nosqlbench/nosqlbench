package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;

import java.net.MalformedURLException;
import java.net.URL;

public class NavigateTo implements WebDriverVerb {

    private final URL url;

    public NavigateTo(String to) {
        try {
            this.url = new URL(to);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(WebContext context) {
        context.driver().navigate().to(url);
    }
}
