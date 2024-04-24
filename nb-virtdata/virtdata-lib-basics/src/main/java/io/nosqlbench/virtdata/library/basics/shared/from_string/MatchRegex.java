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

package io.nosqlbench.virtdata.library.basics.shared.from_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match any input with a regular expression, and apply the associated
 * regex replacement to it, yielding the value.
 * If no matches occur, then the original value is passed through unchanged.
 * Patterns and replacements are passed as even,odd pairs indexed from the
 * 0th position. Back-references to matching groups are supported.

 */
@ThreadSafeMapper
@Categories({Category.general})
public class MatchRegex implements Function<String,String>  {

    private final MatchEntry[] entries;

    @Example({"MatchRegex('.*(25|6to4).*','$1')","Match 25 or 6 to 4 and set the output to only that"})
    @Example({"MatchRegex('([0-9]+)-([0-9]+)-([0-9]+)','$1 $2 $3'", "replaced dashes with spaces in a 10 digit US phone number."})
    @SuppressWarnings("unchecked")
    public MatchRegex(String... specs) {
        if ((specs.length%2)!=0) {
            throw new RuntimeException("You must provide 'pattern1',func1,... for an even number of arguments.");
        }
        entries = new MatchEntry[specs.length/2];
        for (int i = 0; i < specs.length; i+=2) {
            String pattern = specs[i];
            String replacement = specs[i+1];
            entries[i/2]=new MatchEntry(pattern, replacement);
        }
    }

    @Override
    public String apply(String input) {
        for (MatchEntry entry : entries) {
            Matcher m = entry.tryMatch(input);
            if (m!=null) {
                String result = m.replaceAll(entry.replacement);
                return result;
            }
        }
        return input;
    }

    private final static Function<String,String> PASSTHRU = (s) -> s;

    private static class MatchEntry {
        public final Pattern pattern;
        public final String replacement;

        public MatchEntry(String pattern, String replacement) {
            this.pattern = Pattern.compile(pattern);
            this.replacement = replacement;
        }

        public Matcher tryMatch(String s) {
            Matcher matcher = this.pattern.matcher(s);
            if (matcher.matches()) {
                return matcher;
            } else {
                return null;
            }
        }
    }
}
