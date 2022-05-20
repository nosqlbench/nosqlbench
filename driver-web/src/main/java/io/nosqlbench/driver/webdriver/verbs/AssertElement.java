package io.nosqlbench.driver.webdriver.verbs;

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


import io.nosqlbench.driver.webdriver.WebContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class AssertElement implements WebDriverVerb {
    private final static Logger logger = LogManager.getLogger(AssertElement.class);
    private final By by;

    public AssertElement(By by) {
        this.by = by;
    }

    @Override
    public void execute(WebContext context) {
        WebElement element = context.driver().findElement(by);
        context.pushElement(element);
    }

}
