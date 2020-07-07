package io.nosqlbench.driver.webdriver.verbs;

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
