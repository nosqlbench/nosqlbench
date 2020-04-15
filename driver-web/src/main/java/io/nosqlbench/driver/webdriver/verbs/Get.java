package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Get implements WebDriverVerb {
    private final static Logger logger = LoggerFactory.getLogger(Get.class);

    private final String target;

    public Get(String target) {
        this.target = target;
    }

    @Override
    public void execute(WebContext context) {
        context.driver().get(target);
    }

}
