/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.nb.api.config.params;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;


/**
 * <H2>Synopsis</H2>
 * <p>
 * This is a multi-purpose <em>special</em> parser for parameters in a command or
 * other map. It is called special because it does take a few liberties which are not
 * commonly found in regular languages, but which may be desirable for casual use.
 * This is done in a very specific way such that interpretation is unambiguous.
 * </p>
 *
 * <h2>Basic Format</h2>
 * <p>
 * The input line consists of a sequence of names and values, called assignments.
 * These occur as {@code name=value}, and may be delimited by spaces or semicolons, like
 * {@code name1=value1 name2=value2} or {@code name1=value1;name2=value2}.
 * Values can even contain spaces, even though space is a possible delimiter.
 * Thus, <em>{@code name1= value foo bar
 * name2=baz}</em> works, with the values {@code value foo bar} and {@code baz}.
 * </p>
 *
 * <h3>Names</h3>
 * The name of a parameter must appear in raw literal form, with no escaping. A name may not
 * contain spaces, semicolons or equals signs, and may not start with quotes of any kind.
 * Otherwise, there are no restrictions placed on the characters that may appear in
 * a name. Even escaped characters are allowed.
 *
 * <h3>Values</h3>
 * Values can appear as single quoted values, double quoted values, or literal values.
 * The way the value is parsed is determined by the leading character, respectively.
 *
 * <h4>Single Quoted</h4>
 * <p>If the leading character is a single quote, then all following characters up to the next single quote
 * are taken as the value, except for escape characters which work as expected. Double quotes,
 * spaces, equals or any other characters have no special meaning within a single quoted value.
 * </p>
 *
 * <H4>Double Quoted</H4>
 * <p>If the leading character is a double quote, then all following characters up to the next double quote
 * are taken as the value, except for escape characters which work as expected. Single quotes,
 * spaces, equals or other characters have no special meaning within double quotes.</p>
 *
 * <H4>Literal</H4>
 * <p>Otherwise, the value is taken as every single character, including spaces, single quotes,
 * and double quotes, up to the end of the command line or the next parameter assignment.
 * Any occurrence of semicolons which is not escaped will be treated as a delimiter. The next occurrence
 * of a name and equals pattern after a space will be taken as the next named parameter. Otherwise, all
 * spaces and partial word are included in the last value assigment found. Leading spaces on literal
 * values are skipped unless escaped.</p>
 *
 * <H3>Detection</H3>
 * When a caller wants to parse this format optionally when the format is recognizable as having parameters,
 * the {@link #hasValues(String)} method can be called. To be recognized as having parameters, a more strict
 * definition is used: The patter must start with a simple assignment having a varname which starts with an
 * alphabetic character or an underscore, followed by any alpha-numeric, dot {@code .}, dash {@code -}, or
 * underscore {@code _} with an assignment character {@code =} following. The following regex can be used
 * as an example for documentation purposes when explaining op mapping conventions to users:
 * <pre>{@code
 * [_a-zA-Z][-_.\\w]*
 * }</pre>
 */
public class ParamsParser {
    public final static String ASSIGN_CHARS = "=:";
    private final static Logger logger = LogManager.getLogger(ParamsParser.class);

    public static Map<String, String> parse(String input, boolean canonicalize) {
        return parse(input, ASSIGN_CHARS, canonicalize);
    }

    public static boolean hasValues(String input) {
        return hasValues(input, ASSIGN_CHARS);
    }

    public static boolean hasValues(String input, String assignChars) {
        for (int i = 0; i < assignChars.length(); i++) {
            Pattern assignPattern = Pattern.compile("[A-Za-z_][-_\\w\\d.]*\\s*" + assignChars.charAt(i) + ".*");
            if (assignPattern.matcher(input).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parse a string input as a loose-form param=value list, and be reasonable about formatting
     * conventions that most users would follow in the absence of detailed rules.
     * See {@link ParamsParser} for more details on how this works.
     *
     * @param input        The string form containing the parameter names and values to be extracted.
     * @param canonicalize Whether or not to replace synonyms with modern forms and to warn when old forms are used
     * @return A map of extracted keys and values
     */
    public static Map<String, String> parse(String input, String assignmentCharacters, boolean canonicalize) {

        ParseState s = ParseState.expectingName;

        Map<String, String> parms = new LinkedHashMap<>();
        StringBuilder varname = new StringBuilder(512);
        StringBuilder value = new StringBuilder(512);
        String lastVarname = null;
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
                    if (c == '\'' || c == '"') {
                        throw new RuntimeException("Unable to parse a name starting with character '" + c + "'. Names" +
                            " must be literal values.");
                    } else if (c != ' ' && c != ';') {
                        s = ParseState.readingName;
                        varname.append(c);
                    }
                    break;
                case readingName:
                    if (c == '\\') {
                        isEscaped = true;
                    } else if (c == ' ') {
                        String partial = parms.get(lastVarname);
                        if (partial == null) {
                            throw new RuntimeException("space continuation while reading name or value, but no prior " +
                                "for " + lastVarname + " exists");
                        }
                        parms.put(lastVarname, partial + " " + varname);
                        varname.setLength(0);
                    } else if (assignmentCharacters.indexOf(c) == -1) {
                        varname.append(c);
                    } else {
                        s = ParseState.expectingVal;
                    }
                    break;
                case expectingVal:
                    if (c == ' ') {
                    } else if (c == '\\') {
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
                    } else if (c != ';' && c != ' ') {
                        value.append(c);
                    } else {
                        parms.put(varname.toString(), value.toString());
                        lastVarname = varname.toString();
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
            case readingName:
                parms.put(lastVarname, parms.get(lastVarname) + ' ' + varname);
                varname.setLength(0);
                break;
            default:
        }

        if (input.length() > 0 && parms.size() == 0) {
            throw new RuntimeException("Unable to parse input:" + input);
        }

        return parms;
    }

    /**
     * If the source object is a Map, return it as-is. If it is a parseable
     * character sequence with assignments, parse it into a map and return it.
     * Otherwise, if it is a character sequence with no parsable key=value
     * assignments, assume it to be a short-form declaration of a type of
     * selector field like 'name' or 'type', and compose it into a map with
     * the provided main field name.
     *
     * @param src The source Map or CharSequence
     * @param mainField The name of the main field in 'value only' mode.
     * @return A map
     * @throws RuntimeException if the src is neither CharSequence nor Map
     */
    public static Map<String, String> parseToMap(Object src, String mainField) {
        if (src instanceof Map) {
            return (Map) src;
        } else if (src instanceof CharSequence) {
            String input = ((CharSequence) src).toString();
            if (hasValues(input)) {
                return parse(input, false);
            } else {
                return new HashMap<>(Map.of(mainField, input));
            }
        } else {
            throw new RuntimeException("can't parseToMap(...) on an object that is neither Map nor CharSequence, type: '"+src.getClass().getCanonicalName()+"', value:\n" + src);
        }
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
