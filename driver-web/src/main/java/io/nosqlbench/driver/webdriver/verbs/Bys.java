package io.nosqlbench.driver.webdriver.verbs;

import org.openqa.selenium.By;

import java.util.function.Function;

public enum Bys {
    id(By::id),
    linkText(By::linkText),
    name(By::name),
    css(By::cssSelector),
    xpath(By::xpath),
    classname(By::className);

    private Function<String, By> initializer;

    Bys(Function<String,By> initializer) {
        this.initializer = initializer;
    }

    public static By get(String by) {
        Bys bys =classname;
        String[] parts = by.split("=", 2);
        if (parts.length==2) {
            bys= Bys.valueOf(parts[0]);
            by = parts[1];
        }
        return bys.initializer.apply(by);
    }

}
