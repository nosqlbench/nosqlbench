/*
 * Copyright (c) 2024 nosqlbench
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

public enum Fmt {
    Default("", s -> s.length <=2, s -> (s.length==1) ? s[0] : s[0] + ":" + s[1]),
    MapWithEquals("=", s -> s.length == 2, s -> s[0] + "=" + s[1]),
    MapWithColons(":", s -> s.length == 2, s -> s[0] + ":" + s[1]),
    GlobalWithDoubleDashes("--", s -> s.length ==1 && s[0].startsWith("--"), s -> s[0]);

    private final String spec;
    private final Predicate<String[]> validator;
    private final Function<String[], String> formatter;

    Fmt(String spec, Predicate<String[]> validator, Function<String[], String> formatter) {
        this.spec = spec;
        this.validator = validator;
        this.formatter = formatter;
    }

    public static Fmt valueOfSymbol(String s) {
        for (Fmt value : values()) {
            if (value.spec.equals(s)) {
                return value;
            }
        }
        throw new RuntimeException("Format for spec '" + s + "' not found.");
    }

    public void validate(String[] ary) {
        if (!validator.test(ary)) {
            throw new RuntimeException("With fmt '" + this.name() + "': input data not valid for format specifier '" + spec + "': data:[" + String.join("],[",Arrays.asList(ary)) + "]");
        }
    }
    public String format(String[] ary) {
        return formatter.apply(ary);
    }
}
