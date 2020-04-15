package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.WebDriver;

/**
 * All WebDriverVerb implementations are executable commands.
 */
public interface WebDriverVerb {
    /**
     * Execute the command.
     *
     * @param context   The context object allows verbs to do have state from one command to the next. It is a
     *                  {@link ThreadLocal} object which can be modified by any verb.
     */
    void execute(WebContext context);
}
