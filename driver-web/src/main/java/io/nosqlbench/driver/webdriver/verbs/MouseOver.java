package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class MouseOver implements WebDriverVerb {
    private final By by;

    public MouseOver(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        WebDriver driver = context.driver();
        WebElement mouseOverElement = driver.findElement(by);
        Actions actions = new Actions(driver);
        actions.moveToElement(mouseOverElement);
    }
}
