/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.cli.atfiles;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NBAtFileFormats {
    Default("", s -> s.length <= 2, NBAtFileFormats::formatDefaultDashOption),
    MapWithEquals("=", s -> s.length <= 2, NBAtFileFormats::formatNameEqualsValue),
    MapWithColons(":", s -> s.length <= 2, NBAtFileFormats::formatNameColonValue),
    GlobalWithDoubleDashes("--", s -> s.length <= 2 && s[0].startsWith("--"), NBAtFileFormats::formatDashDashOption);

    private static String formatNameEqualsValue(String[] strings) {
        if (strings.length == 2) {
            return strings[0] + "=" + strings[1];
        } else if (strings.length == 1 && strings[0].matches("[a-zA-Z_][a-zA-Z_]*[=:].*")) {
            String[] parts = strings[0].split("[=:]", 2);
            return parts[0]+"="+parts[1];
        } else {
            throw new RuntimeException("Unable to match data for namedd value form: " + String.join("|",Arrays.asList(strings)));
        }
    }

    private static String formatNameColonValue(String[] strings) {
        if (strings.length == 2) {
            return strings[0] + ":" + strings[1];
        } else if (strings.length == 1 && strings[0].matches("[a-zA-Z_][a-zA-Z_]*[=:].*")) {
            String[] parts = strings[0].split("[=:]", 2);
            return parts[0]+":"+parts[1];
        } else {
            throw new RuntimeException("Unable to match data for namedd value form: " + String.join("|",Arrays.asList(strings)));
        }
    }

    private static String formatDefaultDashOption(String[] strings) {
        if (strings.length == 1 && strings[0].startsWith("--")) {
            return formatDashDashOption(strings);
        } else if (strings.length == 1 && strings[0].matches("[a-zA-Z_][a-zA-Z0-9._]*[=:].*")) {
            return formatNameEqualsValue(strings);
        } else if (strings.length == 2) {
            return formatNameEqualsValue(strings);
        } else if (strings.length==1) {
            return strings[0];
        } else {
            throw new RuntimeException("Unable to match data for --global option form: " + String.join("|",Arrays.asList(strings)));
        }
    }

    private final String spec;
    private final Predicate<String[]> validator;
    private final Function<String[], String> formatter;

    NBAtFileFormats(String spec, Predicate<String[]> validator, Function<String[], String> formatter) {
        this.spec = spec;
        this.validator = validator;
        this.formatter = formatter;
    }

    public static NBAtFileFormats valueOfSymbol(String s) {
        for (NBAtFileFormats value : values()) {
            if (value.spec.equals(s)) {
                return value;
            }
        }
        throw new RuntimeException("Format for spec '" + s + "' not found.");
    }

    public void validate(String[] ary) {
        if (!validator.test(ary)) {
            throw new RuntimeException("With fmt '" + this.name() + "': input data not valid for format specifier '" + spec + "': data:[" + String.join("],[", Arrays.asList(ary)) + "]");
        }
    }

    private final static Pattern doubleDashOption = Pattern.compile(
        "^(?<optname>--[a-zA-Z][a-zA-Z0-9_.-]*)((=|\\s+)(?<optvalue>.+))?$"
    );

    private static String formatDashDashOption(String[] words) {

        if (words.length > 1) {
            throw new RuntimeException("too many values for rendering --option in at-file: " + Arrays.asList(words));
        }
        Matcher matcher = doubleDashOption.matcher(words[0]);
        if (matcher.matches()) {
            String optname = matcher.group("optname");
            String optvalue = matcher.group("optvalue");
            if (optvalue!=null) {
                optvalue = (optvalue.matches("'.+'") ? optvalue.substring(1, optvalue.length() - 1) : optvalue);
                optvalue = (optvalue.matches("\".+\"") ? optvalue.substring(1, optvalue.length() - 1) : optvalue);
                return optname + "=" + optvalue;
            } else {
                return optname;
            }
        } else {
            throw new RuntimeException("Unable to match option '" + words[0] + "' with pattern " + doubleDashOption.pattern());
        }
    }

    public String format(String[] ary) {
        return formatter.apply(ary);
    }
}
