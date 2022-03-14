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

package io.nosqlbench.virtdata.lang.ast;

public class StringArg implements ArgType {

    private final String rawEscapedText;
    private final String unEscapedText;

    public StringArg(String rawEscapedText) {
        this.rawEscapedText = rawEscapedText;
        this.unEscapedText = unEscape(rawEscapedText);
    }

    private static String unEscape(String value) {
        String innervalue= value.substring(1, value.length() - 1);
        if (value.startsWith("\"")) {
            innervalue = innervalue.replaceAll("\\\\(.)","$1");
        }
        return innervalue;
    }

    public String getRawValue() {
        return rawEscapedText;
    }
    public String getStringValue() {
        return unEscapedText;
    }

    @Override
    public String toString() {
        return "'"+unEscapedText+"'";
    }
}
