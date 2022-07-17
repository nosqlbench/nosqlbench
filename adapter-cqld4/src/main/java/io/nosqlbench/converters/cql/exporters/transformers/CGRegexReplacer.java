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

package io.nosqlbench.converters.cql.exporters.transformers;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CGRegexReplacer implements Function<String,String>, CGTransformerConfigurable {

    @Override
    public String apply(String s) {
        return null;
    }

    @Override
    public void accept(Map<String, ?> stringMap) {
        List<List<String>> replacers = (List<List<String>>) stringMap.get("replacers");

    }

    private final static class Replacer implements Function<String,String>{
        private final Pattern pattern;
        private final String replacement;

        Replacer(String from, String to) {
            this.pattern = Pattern.compile(from);
            this.replacement = to;
        }

        @Override
        public String apply(String s) {
            Matcher matcher = pattern.matcher(s);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb,replacement);
            }
            matcher.appendTail(sb);
            return sb.toString();
        }

    }


}
