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
import org.openqa.selenium.Dimension;

public class SetWindowSize implements WebDriverVerb {
    private final int width;
    private final int height;

    public SetWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public SetWindowSize(String size) {
        String[] xes = size.split("x");
        this.width = Integer.parseInt(xes[0]);
        this.height = Integer.parseInt(xes[1]);
    }

    @Override
    public void execute(WebContext context) {
        context.driver().manage().window().setSize(new Dimension(width, height));
    }
}
