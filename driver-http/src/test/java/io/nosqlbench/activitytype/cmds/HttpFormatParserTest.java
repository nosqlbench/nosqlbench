package io.nosqlbench.activitytype.cmds;

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
