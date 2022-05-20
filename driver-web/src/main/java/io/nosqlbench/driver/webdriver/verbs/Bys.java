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
