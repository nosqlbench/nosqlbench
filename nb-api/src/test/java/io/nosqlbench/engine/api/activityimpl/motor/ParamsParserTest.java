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

package io.nosqlbench.engine.api.activityimpl.motor;

import io.nosqlbench.nb.api.config.params.ParamsParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ParamsParserTest {

    @Test
    public void testSimpleParams() {
        Map<String, String> p;
        p = ParamsParser.parse("a=1;",true);
        assertThat(p).hasSize(1);
        assertThat(p).containsKey("a");
        assertThat(p.get("a")).isEqualTo("1");
    }

    @Test
    public void testNullValueParam() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=;",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isNull();
    }

    @Test
    public void testSingleQuote() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='fourfive';",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("fourfive");
    }

    @Test
    public void testSingleQuotedEscape() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='fo\\'urfive';",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("fo'urfive");
    }

    @Test
    public void testDoubleQuote() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"six\";",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("six");
    }

    @Test
    public void testDoubleQuotedEscape() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"si\\'x\";",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("si'x");
    }

    @Test
    public void testSQuotesInDQuotes() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b=\"si'x\";",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("si'x");
    }

    @Test
    public void testDQuotesInSquotes() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1;b='Sev\"en';",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("Sev\"en");
    }

    @Test
    public void testSpaces() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1; b=2;",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p.get("a")).isEqualTo("1");
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("2");
    }

    @Test
    public void testMissingSemi() {
        Map<String,String> p;
        p = ParamsParser.parse("a=1; b=2",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("b");
        assertThat(p.get("b")).isEqualTo("2");
    }

    @Test
    public void testSpaceDelimiter() {
        Map<String, String> p = ParamsParser.parse("a=1 b=2",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p).containsKey("b");
        assertThat(p.get("a")).isEqualTo("1");
        assertThat(p.get("b")).isEqualTo("2");
    }

    @Test
    public void testSpaceDelimiterGappedFirst() {
        Map<String, String> p = ParamsParser.parse("a=1 2 3 b=2",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p).containsKey("b");
        assertThat(p.get("a")).isEqualTo("1 2 3");
        assertThat(p.get("b")).isEqualTo("2");
    }

    @Test
    public void testSpaceDelimiterGappedLast() {
        Map<String, String> p = ParamsParser.parse("a=1 b=2 3 4",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p).containsKey("b");
        assertThat(p.get("a")).isEqualTo("1");
        assertThat(p.get("b")).isEqualTo("2 3 4");
    }

    @Test
    public void testInvalidNameException() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> ParamsParser.parse("a=1\\\\;'\";b=2 3 4",true));
    }

    @Test
    public void testSkippingLiteralLeadingSpaces() {
        Map<String, String> p = ParamsParser.parse("a= fiver b=\\ sixer",true);
        assertThat(p).hasSize(2);
        assertThat(p).containsKey("a");
        assertThat(p).containsKey("b");
        assertThat(p.get("a")).isEqualTo("fiver");
        assertThat(p.get("b")).isEqualTo(" sixer");
    }

    @Test
    public void testHasValues() {
        assertThat(ParamsParser.hasValues("has=values")).isTrue();
        assertThat(ParamsParser.hasValues("has = values")).isTrue();
        assertThat(ParamsParser.hasValues("has =values")).isTrue();
        assertThat(ParamsParser.hasValues("has= values")).isTrue();
        assertThat(ParamsParser.hasValues("3has= values")).isFalse();
        assertThat(ParamsParser.hasValues("_has= values")).isTrue();
        assertThat(ParamsParser.hasValues("h-as= values")).isTrue();
        assertThat(ParamsParser.hasValues("h.as= values")).isTrue();
        assertThat(ParamsParser.hasValues("h_as= values")).isTrue();
    }

}
