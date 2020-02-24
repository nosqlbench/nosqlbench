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

import java.util.LinkedHashMap;
import java.util.Map;


public class ParamsParser {

    public static Map<String, String> parse(String input) {
        ParseState s = ParseState.expectingName;

        Map<String, String> parms = new LinkedHashMap<>();
        StringBuilder varname = new StringBuilder(128);
        StringBuilder value = new StringBuilder(128);
        boolean isEscaped = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (isEscaped) {
                switch (s) {
                    case expectingVal:
                    case readingDquotedVal:
                    case readingSquotedVal:
                    case readingRawVal:
                        value.append(c);
                        break;
                    case readingName:
                        varname.append(c);
                        break;
                    default:
                        throw new RuntimeException("invalid position for escape:" + i + ", in " + input);
                }
                isEscaped = false;
                continue;
            }

            switch (s) {
                case expectingName:
                    if (c != ' ' && c != ';') {
                        s = ParseState.readingName;
                        varname.append(c);
                    }
                    break;
                case readingName:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c != '=') {
                        varname.append(c);
                    } else {
                        s = ParseState.expectingVal;
                    }
                    break;
                case expectingVal:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c == '\'') {
                        s = ParseState.readingSquotedVal;
                    } else if (c == '"') {
                        s = ParseState.readingDquotedVal;
                    } else if (c == ';') {
                        parms.put(varname.toString(), null);
                        varname.setLength(0);
                        s = ParseState.expectingName;
                    } else {
                        s = ParseState.readingRawVal;
                        value.append(c);
                    }
                    break;
                case readingRawVal:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c != ';') {
                        value.append(c);
                    } else {
                        parms.put(varname.toString(), value.toString());
                        varname.setLength(0);
                        value.setLength(0);
                        s = ParseState.expectingName;
                    }
                    break;
                case readingSquotedVal:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c != '\'') {
                        value.append(c);
                    } else {
                        parms.put(varname.toString(), value.toString());
                        varname.setLength(0);
                        value.setLength(0);
                        s = ParseState.expectingName;
                    }
                    break;
                case readingDquotedVal:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c != '"') {
                        value.append(c);
                    } else {
                        parms.put(varname.toString(), value.toString());
                        varname.setLength(0);
                        value.setLength(0);
                        s = ParseState.expectingName;
                    }
                    break;
                default:
                    throw new RuntimeException("Unmatched parse state. This should be impossible.");
            }
        }

        if (isEscaped) {
            throw new RuntimeException("an unfinished escape sequence at the end is not valid");
        }

        switch (s) {
            case expectingVal:
                parms.put(varname.toString(), null);
                varname.setLength(0);
                s = ParseState.expectingName;
                break;
            case readingRawVal:
                parms.put(varname.toString(), value.toString());
                varname.setLength(0);
                s = ParseState.expectingName;
                break;
            default:
        }

        if (input.length()>0 && parms.size()==0) {
            throw new RuntimeException("Unable to parse input:" + input);
        }

        return parms;
    }

    private enum ParseState {
        expectingName,
        readingName,
        expectingVal,
        readingRawVal,
        readingSquotedVal,
        readingDquotedVal
    }
}
