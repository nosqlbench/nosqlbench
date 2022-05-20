package io.nosqlbench.engine.clients.grafana;

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


import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GStitcher {

    private final Map<String, Set<String>> values;
    private final static Pattern pattern = Pattern.compile("\\$(\\w+)");

    public GStitcher(Map<String, Set<String>> values) {
        this.values = values;
    }

    public String stitchRegex(String spec) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = pattern.matcher(spec);
        while (matcher.find()) {
            String word = matcher.group(1);
            if (values.containsKey(word)) {
                Set<String> elems = values.get(word);
                String replacement = "(" + String.join("|", elems) + ")";
                matcher.appendReplacement(sb, replacement);
            } else {
                matcher.appendReplacement(sb, "NOTFOUND[" + word + "]");
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
