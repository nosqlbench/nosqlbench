package io.nosqlbench.activitytype.cmds;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpFormatParserTest {

    @Test
    public void testSimpleFormat() {
        Map<String, String> map = HttpFormatParser.parseInline("GET http://host:12345/path/{\"v\":{\"name\":\"value\"}");
        assertThat(map).containsAllEntriesOf(
            Map.of("method", "GET", "uri", "http://host:12345/path/{\"v\":{\"name\":\"value\"}")
        );
    }

    @Test
    public void testUrlEncodeBasic() {
        Map<String, String> map2 = HttpFormatParser.parseInline("GET http://host:12345/path/E[[{\"v\":{\"name\":\"value\"}]]");
        assertThat(map2).containsAllEntriesOf(
            Map.of("method", "GET", "uri", "http://host:12345/path/%7B%22v%22%3A%7B%22name%22%3A%22value%22%7D"));
    }

    @Test
    public void testUrlEncodeExceptBindings() {
        Map<String, String> map3 = HttpFormatParser.parseInline("GET http://host:12345/path/E[[{\"v\":{\"{name}\":\"{value}\"}]]");
        assertThat(map3).containsAllEntriesOf(
            Map.of("method", "GET", "uri", "http://host:12345/path/%7B%22v%22%3A%7B%22{name}%22%3A%22{value}%22%7D"));
    }

}
