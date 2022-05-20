package io.nosqlbench.driver.webdriver;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
