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


import org.openqa.selenium.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class WebContext {
    private final static Logger logger = LogManager.getLogger(WebContext.class);

    private final WebDriver rootwindow;
    WebDriver focus;

    LinkedList<WebElement> elements = new LinkedList<>();
    private final LinkedList<Cookie> cookies = new LinkedList<>();
    private final HashMap<String,Object> vars = new HashMap<>();
    private Alert alert;

    public WebContext(WebDriver initial) {
        this.focus = initial;
        this.rootwindow = initial;
    }

    public void pushElement(WebElement element) {
        elements.push(element);
    }

    public WebElement peekElement() {
        return elements.peek();
    }

    public LinkedList<Cookie> getCookies() {
        return cookies;
    }

    public LinkedList<WebElement> getElements() {
        return elements;
    }

    public void setElements(List<WebElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    public void pushCookie(Cookie cookie) {
        this.cookies.push(cookie);
    }

    public void setCookies(Collection<Cookie> cookies) {
        this.cookies.clear();
        this.cookies.addAll(cookies);

    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public Alert getAlert() {
        return alert;
    }

    public WebDriver driver() {
        return focus;
    }

    public void setFocus(WebDriver driver) {
        this.focus =driver;
    }

    public void clearAlert() {
        this.alert=null;
    }

    public void setVar(String key, Object value) {
        this.vars.put(key,value);
        logger.debug("context vars: '" + key + "'='" + value.toString() + "'");
    }

    public <T> T getVar(String name, Class<? extends T> type) {
        Object o = this.vars.get(name);
        if (o==null) { return null; }

        if (type.isAssignableFrom(o.getClass())) {
            return type.cast(o);
        }
        throw new RuntimeException("Could not cast named var '" + name + "' to a " + type.getCanonicalName());
    }
}
