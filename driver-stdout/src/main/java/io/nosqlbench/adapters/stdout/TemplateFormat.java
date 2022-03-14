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

package io.nosqlbench.adapters.stdout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum TemplateFormat {

    csv("value1,value2,..."),
    assignments("varname1=value1 varname2=value2 ..."),
    readout("varname1       : value1\nvarname2:      : value2\n..."),
    json("{\n varname1: value1,\n varname2: value2,\n ...\n}\n"),
    inlinejson("{varname1:value1, varname2:value2, ...}");

    private final String example;

    TemplateFormat(String example) {
        this.example = example;
    }

    public String format(boolean addNewlineSeparator, List<String> fieldNames) {
        return this.format(addNewlineSeparator, fieldNames, null);
    }

    public String format(
            boolean addNewlineSeparator,
            List<String> fieldNames,
            Function<String, String> fieldNameAdapter) {

        if (fieldNameAdapter != null) {
            fieldNames = fieldNames.stream()
                    .map(fieldNameAdapter)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        String template = "";
        switch (this) {
            case csv:
                template = fieldNames
                        .stream().map(s -> "{" + s + "}")
                        .collect(Collectors.joining(","));
                break;
            case assignments:
                template = fieldNames
                        .stream().map(s -> s + "={" + s + "}")
                        .collect(Collectors.joining(" "));
                break;
            case readout:
                int maxlen = fieldNames.stream().mapToInt(String::length).max().orElse(0);
                template = fieldNames
                        .stream().map((field) -> String.format("%" + maxlen + "s : {%s}", field, field))
                        .collect(Collectors.joining("\n"));
                break;
            case json:
                template = fieldNames
                        .stream().map(s -> "\"" + s + "\":\"{" + s + "}\"")
                        .collect(Collectors.joining(",\n ", "{\n ", "\n}"));
                break;
            case inlinejson:
                template = fieldNames
                        .stream().map(s -> "\""+ s + "\":\"{" + s + "}\"")
                        .collect(Collectors.joining(", ", "{", "}"));
                break;
            default:
                throw new RuntimeException("No supported format was found for " + this);
        }

        if (addNewlineSeparator) {
            template = withSeparator(template);
        }
        return template;
    }

    /**
     * Ensure that the statement ends with a newline.
     * If there are newlines within the statement, then ensure that it ends with a double newline.
     * @param raw statement possibly without newlines
     * @return statement with appropriate newlines
     */
    private String withSeparator(String raw) {
        int pos = raw.indexOf("\n");
        if (pos>0 && pos<raw.length()-1) {
            if (raw.endsWith("\n\n")) {
                return raw;
            }
            if (raw.endsWith("\n")) {
                return raw + "\n";
            }
            return raw+"\n\n";
        }
        if (!raw.endsWith("\n")) {
            return raw+"\n";
        }
        return raw;
    }
}
