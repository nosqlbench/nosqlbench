/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.nb.api.errors.BasicError;

import java.util.*;


/**
 * <P>Take zero or more strings containing combined argv and return
 * a single {@link Cmd} list containing zero or more commands.</P>
 * <p>
 * {@see <a href="https://pubs.opengroup.org/onlinepubs/9699919799/functions/wordexp.html">POSIX wordexp</a>},
 * of which this is a shallow substitute.</P>
 */
public class CmdParser {
    public final static String SYMBOLS ="`~!@#$%^&*(){}[]|+?!";

    public static List<Cmd> parse(String... strings) {
        List<Cmd> cmds = new LinkedList<>();
        for (String string : strings) {
            cmds.addAll(parseCmdString(string));
        }
        return cmds;
    }

    public static enum PS {
        barename_start, barename, equals, barevalue, dquote, squote, end
    }

    private static List<? extends Cmd> parseCmdString(String line) {
        List<Cmd> cmds = new LinkedList<>();
        LinkedHashMap<String,String> params = new LinkedHashMap<>();
        String cmdType = null;
        String varname = "", value="";
        StringBuilder buf = new StringBuilder(1024);
        PS state = PS.barename_start;
        int pos = -1;
        while (state != PS.end) {
            char at = 0;
            CharType type;
            if (++pos >= line.length()) {
                type = CharType.EOI;
            } else {
                at = line.charAt(pos);
                type = CharType.of(at);
            }
            if (type == CharType.unknown) throw new BasicError("Unknown character class for '" + at + "'");

            state = switch (state) {
                case barename_start -> switch (type) {
                    case space -> PS.barename_start;
//                    case dquote -> PS.dquote;
//                    case squote -> PS.squote;
                    case alpha,numeric,symbol -> {
                        buf.setLength(0);
                        buf.append(at);
                        yield PS.barename;
                    }
                    case EOI -> {
                        if (cmdType!=null) {
                            cmds.add(new Cmd(cmdType,params));
                        }
                        yield PS.end;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case barename -> switch (type) {
                    case alpha,numeric -> {
                        buf.append(at);
                        yield PS.barename;
                    }
                    case space -> {
                        if (cmdType!=null) {
                            // now that we see the next command head, time to bank the last one and re-use the typename
                            cmds.add(new Cmd(cmdType,params));
                        }
                        params=new LinkedHashMap<>();
                        cmdType = buf.toString().trim();
                        buf.setLength(0);
                        yield PS.barename_start;
                    }
                    case EOI -> {
                        cmds.add(new Cmd(buf.toString().trim(), params));
                        yield PS.end;
                    }
                    case equals -> {
                        if (cmdType==null) PS_error(at, pos, state, type,"parameter found while no command has been specified");
                        varname=buf.toString();
                        buf.setLength(0);
                        yield PS.equals;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case barevalue -> switch (type) {
                    case space -> {
                        value = buf.toString();
                        params.put(varname, value);
                        buf.setLength(0);
                        yield PS.barename_start;
                    }
                    case EOI -> {
                        value = buf.toString();
                        params.put(varname, value);
                        cmds.add(new Cmd(cmdType,params));
                        yield PS.end;
                    }
                    default -> {
                        buf.append(at);
                        yield PS.barevalue;
                    }
                };
                case dquote -> switch (type) {
                    case dquote -> {
                        value = buf.toString();
                        buf.setLength(0);
                        params.put(varname,value);
                        yield PS.barename_start;
                    }
                    case EOI -> PS_error(at, pos, state, type, "reached end of input while reading double-quoted value");
                    default -> {
                        buf.append(at);
                        yield PS.dquote;
                    }
                };
                case squote -> switch (type) {
                    case squote -> {
                        value = buf.toString();
                        buf.setLength(0);
                        params.put(varname,value);
                        yield PS.barename_start;
                    }
                    case EOI -> PS_error(at, pos, state, type, "reached end of input while reading single-quoted value");
                    default -> {
                        buf.append(at);
                        yield PS.squote;
                    }
                };
                case equals -> switch (type) {
                    case dquote -> {
                        buf.setLength(0);
                        yield PS.dquote;
                    }
                    case squote -> {
                        buf.setLength(0);
                        yield PS.squote;
                    }
                    case alpha,numeric,underscore,symbol -> {
                        buf.setLength(0);
                        buf.append(at);
                        yield PS.barevalue;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case end -> throw new RuntimeException("Invalid fallthrough to end state. This should have been skipped");

            };


        }
        ;
        return cmds;

    }

    private static PS PS_error(char at, int pos, PS state, CharType type, String... msg) {
        throw new BasicError("invalid char '" + at + "' at position " + pos + " in parser state '" + state + "'"
            +(type!=null ? " for type '" + type.name() + "'" : "")
            +((msg.length>0) ? " " + String.join(", ", Arrays.asList(msg)):""));
    }

    private static enum CharType {
        alpha,
        numeric,
        squote,
        dquote,
        equals,
        underscore,
        space,
        newline,
        symbol,
        EOI,
        unknown;

        static CharType of(char c) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return alpha;
            if ((c >= '0' && c <= '9')) return numeric;
            if (c == '_') return underscore;
            if (c == '=') return equals;
            if (c == '\'') return squote;
            if (c == '"') return dquote;
            if (c == ' ' || c == '\t') return space;
            if (c == '\n' || c == '\r') return newline;
            if (SYMBOLS.indexOf(c)>=0) return symbol;
            return unknown;
        }
    }
}
