/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.cqlgen.exporter.transformers;

import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class CGRegexReplacerTest {

    @Test
    public void testErroredMatcher() {
//        Pattern pattern = Pattern.compile("(?m)(?s)(?i)(\\s*PRIMARY KEY .*?)\\b(options|role|roles|permissions|permission|date|key|timestamp|type|keys)\\b(.*)$");
        Pattern pattern1 = Pattern.compile("(?m)(?i)^(\s*PRIMARY KEY.*?)\\b(options|role|roles|permissions|permission|date|key|timestamp|type|keys)\\b(.*?)$");
        String tomatch = """
            CREATE TABLE dse_insights_local.insights_config (
                r__key int,
                config dse_insights_local.insights_config_type,
                PRIMARY KEY (key)
                PRIMARY KEY (roles)
                PRIMARY KEY (foo, bar,timestamp, baz)
                 options map<text, text>,
            ) WITH read_repair_chance = 0.0
            """;
        Matcher matcher = pattern1.matcher(tomatch);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb,"$1<$2>$3");
        }
        matcher.appendTail(sb);
        String phase1 = sb.toString();
        assertThat(phase1).isEqualTo("""
            CREATE TABLE dse_insights_local.insights_config (
                r__key int,
                config dse_insights_local.insights_config_type,
                PRIMARY KEY (<key>)
                PRIMARY KEY (<roles>)
                PRIMARY KEY (foo, bar,<timestamp>, baz)
                 options map<text, text>,
            ) WITH read_repair_chance = 0.0
            """);

        sb.setLength(0);

        Pattern pattern2 = Pattern.compile(
            "(?m)(?i)^(\\s*?)\\b(options)\\b(\\s+[a-zA-Z][a-zA-Z<>_, -]*?,?)$"
        );
        Matcher matcher2 = pattern2.matcher(phase1);
        while (matcher2.find()) {
            matcher2.appendReplacement(sb,"$1{$2}$3");
        }
        matcher2.appendTail(sb);
        String phase2=sb.toString();
        assertThat(phase2).isEqualTo("""
            CREATE TABLE dse_insights_local.insights_config (
                r__key int,
                config dse_insights_local.insights_config_type,
                PRIMARY KEY (<key>)
                PRIMARY KEY (<roles>)
                PRIMARY KEY (foo, bar,<timestamp>, baz)
                 {options} map<text, text>,
            ) WITH read_repair_chance = 0.0
            """);


//        assertThat(matcher).matches();
//        System.out.println(matcher.group(0));
//        System.out.println(matcher.group(1));
//        System.out.println(matcher.group(2));
//        System.out.println(matcher.group(3));
    }

}
