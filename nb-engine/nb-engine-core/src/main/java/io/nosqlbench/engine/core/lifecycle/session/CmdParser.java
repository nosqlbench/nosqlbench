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

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.cmdstream.CmdArg;
import io.nosqlbench.nb.api.errors.BasicError;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <P>Take zero or more strings containing combined argv and return
 * a single {@link Cmd} list containing zero or more commands.</P>
 * <p>
 * {@see <a href="https://pubs.opengroup.org/onlinepubs/9699919799/functions/wordexp.html">POSIX wordexp</a>},
 * of which this is a shallow substitute.</P>
 */
public class CmdParser {
    public final static String SYMBOLS = "`~!@#$%^&*(){}[]|+?!";

    public static List<Cmd> parse(String... strings) {
        List<Cmd> cmds = new LinkedList<>();
        for (String string : strings) {
            cmds.addAll(parseCmdString(string));
        }
        return cmds;
    }

    private record parameter(String name, String op, String value) {}
    private record command(String name){}
    private final static Pattern combinedPattern =
        Pattern.compile("(?<varname>[a-zA-Z_][a-zA-Z0-9_.-]*)(?<operator>=+)(?<value>.+)|(?<command>[a-zA-Z_][a-zA-Z0-9_.]*)",Pattern.DOTALL);
    private final static Pattern commandName =Pattern.compile("^$");
    public static LinkedList<Cmd> parseArgvCommands(LinkedList<String> args) {
        LinkedList<Record> cmdstructs = new LinkedList<>();
        LinkedList<Cmd> cmds = new LinkedList<>();

        while (!args.isEmpty()) {
            String arg=args.peekFirst();
            Matcher matcher = combinedPattern.matcher(arg);
            if (matcher.matches()) {
                args.removeFirst();
                String command = matcher.group("command");
                String varname = matcher.group("varname");
                String operator = matcher.group("operator");
                String value = matcher.group("value");
                cmdstructs.add(command!=null ? new command(command) : new parameter(varname,operator,value));
            } else {
                break;
//                throw new BasicError("Unable to parse arg as a command or an assignment: '"+arg+"'");
            }
        }
        while (!cmdstructs.isEmpty()) {
            if (cmdstructs.peekFirst() instanceof command cmd) {
                cmdstructs.removeFirst();
                Map<String,CmdArg> params = new LinkedHashMap<>();
                while (cmdstructs.peekFirst() instanceof parameter param) {
                    cmdstructs.removeFirst();
                    if (params.containsKey(param.name())) {
                        throw new BasicError("Duplicate occurrence of option: " + param.name());
                    }
                    params.put(param.name(),CmdArg.of(cmd.name(),param.name(),param.op(),param.value()));
                }
                cmds.add(new Cmd(cmd.name(),params));
            } else {
                throw new BasicError("first word in argv is not a command: '" + cmdstructs.peekFirst() + "'");
            }
        }
        return cmds;
    }

    public static enum PS {
        barename_start, barename, equals, barevalue, dquote, squote, end
    }

    private static List<? extends Cmd> parseCmdString(String line) {
        List<Cmd> cmds = new LinkedList<>();
        LinkedHashMap<String, CmdArg> args = new LinkedHashMap<>();
        String cmdName = null;
        String varname = null, equals = null, value = null;
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
                    case alpha, underscore -> {
                        buf.setLength(0);
                        buf.append(at);
                        yield PS.barename;
                    }
                    case EOI -> {
                        if (cmdName != null) {
                            cmds.add(new Cmd(cmdName, args));
                        }
                        yield PS.end;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case barename -> switch (type) {
                    case alpha, numeric, underscore, dot -> {
                        buf.append(at);
                        yield PS.barename;
                    }
                    case space -> {
                        if (cmdName != null) cmds.add(new Cmd(cmdName, args));
                        args = new LinkedHashMap<>();
                        cmdName = buf.toString().trim();
                        buf.setLength(0);
                        yield PS.barename_start;
                    }
                    case EOI -> {
                        cmds.add(new Cmd(buf.toString().trim(), args));
                        yield PS.end;
                    }
                    case equals -> {
                        if (cmdName == null)
                            PS_error(at, pos, state, type, "parameter found while no command has been specified");
                        varname = buf.toString();
                        buf.setLength(0);
                        buf.append(at);
                        yield PS.equals;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case barevalue -> switch (type) {
                    case space -> {
                        value = buf.toString();
                        args.put(varname, CmdArg.of(cmdName, varname, equals, value));
                        varname = null;
                        equals = null;
                        buf.setLength(0);
                        yield PS.barename_start;
                    }
                    case EOI -> {
                        value = buf.toString();
                        args.put(varname, CmdArg.of(cmdName, varname, equals, value));
                        cmds.add(new Cmd(cmdName, args));
                        varname = null;
                        equals = null;
                        buf.setLength(0);
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
                        args.put(varname, CmdArg.of(cmdName, varname, equals, value));

                        varname = null;
                        equals = null;
                        buf.setLength(0);

                        yield PS.barename_start;
                    }
                    case EOI ->
                        PS_error(at, pos, state, type, "reached end of input while reading double-quoted value");
                    default -> {
                        buf.append(at);
                        yield PS.dquote;
                    }
                };
                case squote -> switch (type) {
                    case squote -> {

                        value = buf.toString();
                        args.put(varname, CmdArg.of(cmdName, varname, equals, value));
                        varname = null;
                        equals = null;
                        buf.setLength(0);
                        yield PS.barename_start;
                    }
                    case EOI ->
                        PS_error(at, pos, state, type, "reached end of input while reading single-quoted value");
                    default -> {
                        buf.append(at);
                        yield PS.squote;
                    }
                };
                case equals -> switch (type) {
                    case equals -> {
                        buf.append(at);
                        yield PS.equals;
                    }
                    case dquote -> {
                        equals = buf.toString();
                        buf.setLength(0);
                        yield PS.dquote;
                    }
                    case squote -> {
                        equals = buf.toString();
                        buf.setLength(0);
                        yield PS.squote;
                    }
                    case alpha, numeric, underscore, symbol -> {
                        equals = buf.toString();
                        buf.setLength(0);
                        buf.append(at);
                        yield PS.barevalue;
                    }
                    default -> PS_error(at, pos, state, type);
                };
                case end ->
                    throw new RuntimeException("Invalid fallthrough to end state. This should have been skipped");
            };
        }
        return cmds;

    }

    private static PS PS_error(char at, int pos, PS state, CharType type, String... msg) {
        throw new BasicError("invalid char '" + at + "' at position " + pos + " in parser state '" + state + "'"
            + (type != null ? " for type '" + type.name() + "'" : "")
            + ((msg.length > 0) ? " " + String.join(", ", Arrays.asList(msg)) : ""));
    }

    private static enum CharType {
        alpha,
        numeric,
        squote,
        dquote,
        equals,
        underscore,
        space,
        dot,
        newline,
        symbol,
        EOI,
        unknown;

        static CharType of(char c) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return alpha;
            if ((c >= '0' && c <= '9')) return numeric;
            if (c == '_') return underscore;
            if (c == '.') return dot;
            if (c == '=') return equals;
            if (c == '\'') return squote;
            if (c == '"') return dquote;
            if (c == ' ' || c == '\t') return space;
            if (c == '\n' || c == '\r') return newline;
            if (SYMBOLS.indexOf(c) >= 0) return symbol;
            return unknown;
        }
    }
}
