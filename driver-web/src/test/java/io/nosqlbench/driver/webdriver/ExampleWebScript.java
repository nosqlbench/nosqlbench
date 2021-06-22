package io.nosqlbench.driver.webdriver;

import io.nosqlbench.nb.api.testutils.Perf;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;


/**
 * References:
 * <UL>
 *     <LI>https://www.guru99.com/selenium-with-htmlunit-driver-phantomjs.html</LI>
 *     <LI></LI>
 * </UL>
 */
public class ExampleWebScript {

    @Test
    @Disabled
    public void getDocsSiteChromeDriver() {
        System.setProperty("webdriver.http.factory", "okhttp");
        WebDriver driver = new ChromeDriver();
        Perf perf = new Perf("chrome");
        int ops = 100;
        try (Perf.Time time = perf.start("chrome",ops)) {
            for (int i = 1; i <= ops; i++) {
                System.out.print(".");
                if ((i%10)==0) { System.out.println(i); }
                driver.get("http://localhost:6081");
            }
        }
        System.out.println("!");

        System.out.println(perf);
//        driver.get("http://docs.nosqlbench.io/");
        driver.close();
    }

    @Test
    @Disabled
    public void getDocSiteWebHtml() {
//        System.setProperty("webdriver.http.factory", "okhttp");
        WebDriver driver = new HtmlUnitDriver(false);
        Perf perf = new Perf("htmlunit");
        int ops = 100;
        try (Perf.Time time = perf.start("htmlunit",ops)) {
            for (int i = 1; i <= ops; i++) {
                System.out.print(".");
                if ((i%10)==0) { System.out.println(i); }
                driver.get("http://localhost:6081");
            }
        }
        System.out.println("!");

        System.out.println(perf);
//        driver.get("http://docs.nosqlbench.io/");
        driver.close();
    }
}
