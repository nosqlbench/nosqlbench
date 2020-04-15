package io.nosqlbench.driver.webdriver.verbs;

import io.nosqlbench.driver.webdriver.WebContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class MouseOut implements WebDriverVerb {
    @Override
    public void execute(WebContext context) {
        WebDriver driver = context.driver();
        WebElement element = driver.findElement(By.tagName("body"));
        Actions actions = new Actions(driver);
        actions.moveToElement(element, 0, 0).perform();
    }
}
