/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.activityimpl.motor;

import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Test
public class ParamsParserTest {

    @Test
    public void testSimpleParams() {
        Map<String, String> p;
        p = ParamsParser.parse("a=1;");
        assertThat(p).hasSize(1);
        assertThat(p).containsKey("a");
        assertThat(p.get("a")).isEqualTo("1");
    }

    @Test
    public void testNullValueParam() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=;");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isNull();
    }

    @Test
    public void testSingleQuote() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='fourfive';");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("fourfive");
    }

    @Test
    public void testSingleQuotedEscape() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='fo\\'urfive';");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("fo'urfive");

    }

    @Test
    public void testDoubleQuote() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"six\";");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("six");
    }

    @Test
    public void testDoubleQuotedEscape() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"si\\'x\";");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("si'x");
    }

    @Test
    public void testSQuotesInDQuotes() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"si'x\";");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("si'x");
    }

    @Test
    public void testDQuotesInSquotes() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='Sev\"en';");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("Sev\"en");
    }

    @Test
    public void testSpaces() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1; b=2;");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p.get("a")).isEqualTo("1");
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("2");
    }

    @Test
    public void testMissingSemi() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1; b=2");
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("2");
    }


}
