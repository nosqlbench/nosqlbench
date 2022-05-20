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


import org.openqa.selenium.Cookie;

import java.sql.Date;
import java.util.Map;
import java.util.Optional;

public class CookieJar {
    public static Cookie cookie(Map<String,String> props) {
        String name = props.get("name");
        String value = props.getOrDefault("value","");
        Cookie.Builder builder = new Cookie.Builder(name, value);
        Optional.ofNullable(props.get("domain")).ifPresent(builder::domain);
        Optional.ofNullable(props.get("expiry")).map(Date::valueOf).ifPresent(builder::expiresOn);
        Optional.ofNullable(props.get("ishttponly")).map(Boolean::valueOf).ifPresent(builder::isHttpOnly);
        Optional.ofNullable(props.get("issecure")).map(Boolean::valueOf).ifPresent(builder::isSecure);
        Optional.ofNullable(props.get("path")).ifPresent(builder::path);
        return builder.build();
    }

}
