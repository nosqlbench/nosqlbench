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

package io.nosqlbench.engine.cli;

import io.nosqlbench.engine.core.lifecycle.session.CmdParser;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.function.Function;

/**
 * Encapsulate Command parsing and structure for the NoSQLBench command line. Commands always have a name, sometimes
 * have a list of positional arguments, and sometimes have a map of named parameters. An example of a command tha thas
 * both would look like {@code script test.js p1=v1}
 */
public class Cmd {

    private final static Logger logger = LogManager.getLogger(Cmd.class);
    public static final String DEFAULT_TARGET_CONTEXT = "default";
    private final String targetContextName;
    private final String stepName;

    public String getTargetContext() {
        return targetContextName;
    }

    public Cmd forTargetContext(String contextName, String stepName) {
        return new Cmd(cmdType,cmdArgs,contextName, stepName);
    }

    public enum CmdType {
        run(),
        start(),
        stop(Arg.of("alias_name")),
        forceStop(Arg.of("alias_name")),
        script(Arg.of("script_path", s -> s)),
        java(Arg.of("main_class",s->s)),
        await(Arg.of("alias_name")),
        waitMillis(Arg.of("millis_to_wait", Long::parseLong)),
        fragment(Arg.ofFreeform("script_fragment")),
        context(Arg.ofFreeform("context_name")),
        indirect(Arg.of("name"));

        private final Arg<?>[] positional;
        CmdType(Arg<?>... positional) {
            this.positional = positional;
        }

        public static boolean anyMatches(String s) {
            CmdType found = valueOfAnyCaseOrIndirect(s);
            return found!=null;
        }

        public String[] getPositionalArgNames() {
            String[] names = new String[positional.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = positional[i].name;
            }
            return names;
        }

        public static CmdType valueOfAnyCaseOrIndirect(String cmdname) {
            for (CmdType value : values()) {
                if (cmdname.equals(value.toString()) || cmdname.equalsIgnoreCase(value.toString())) {
                    return value;
                }
            }
            return indirect;
        }

        public Arg<?>[] getPositionalArgs() {
            return positional;
        }
    }

    private static final class Arg<T> {
        public final String name;
        public final Function<String, T> converter;
        public final boolean freeform;

        public Arg(String name, Function<String, T> converter, boolean freeform) {
            this.name = name;
            this.converter = converter;
            this.freeform = freeform;
        }

        public static <T> Arg<T> of(String name, Function<String, T> converter) {
            return new Arg<>(name, converter, false);
        }

        public static Arg<String> of(String name) {
            return new Arg<>(name, s -> s, false);
        }

        public static Arg<String> ofFreeform(String name) {
            return new Arg<>(name, s -> s, true);
        }
    }


    private final Map<String, String> cmdArgs;

    public String getArg(String paramName) {
        return this.cmdArgs.get(paramName);
    }

    private final CmdType cmdType;

    public Cmd(@NotNull CmdType cmdType, Map<String, String> cmdArgs, String targetContextName, String stepName) {
        this.cmdArgs = cmdArgs;
        this.cmdType = cmdType;
        this.stepName = stepName;
        this.targetContextName = targetContextName;
    }

    public Cmd(@NotNull String cmdTypeOrName, Map<String,String> argmap) {
        this.cmdType = CmdType.valueOfAnyCaseOrIndirect(cmdTypeOrName);
        this.targetContextName = DEFAULT_TARGET_CONTEXT;
        this.stepName = "no-step";
        this.cmdArgs = new LinkedHashMap<>(argmap);
        if (this.cmdType==CmdType.indirect) {
            this.cmdArgs.put("_name", cmdTypeOrName);
        }
    }
    public Cmd(@NotNull CmdType cmdType, Map<String, String> cmdArgMap) {
        this.cmdArgs = new LinkedHashMap<>(cmdArgMap);
        this.cmdType = cmdType;
        this.targetContextName = DEFAULT_TARGET_CONTEXT;
        this.stepName = "no-step";
        if (cmdType==CmdType.indirect && !cmdArgMap.containsKey("_name")) {
            throw new RuntimeException("indirect cmd type is invalid without a '_name' parameter.");
        }
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    public Map<String, String> getParams() {
        return cmdArgs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdType.toString());
        getParams().forEach((k,v) -> {
            String sanitizedValue = v;

            for (char c :v.toCharArray()){
                if (CmdParser.SYMBOLS.indexOf(c)>=0) {
                    sanitizedValue = "'" + v +"'";
                    break;
                }
            }
            sb.append(" ").append(k).append("=").append(sanitizedValue);
        });
        return sb.toString();
    }

    public static Cmd parseArg(LinkedList<String> arglist, PathCanonicalizer fixer) {

        String cmdName = arglist.removeFirst();
        CmdType cmdType = CmdType.valueOfAnyCaseOrIndirect(cmdName);

        Map<String, String> params = new LinkedHashMap<>();

        for (Arg<?> arg : cmdType.getPositionalArgs()) {

            String nextarg = arglist.peekFirst();

            if (nextarg == null) {
                throw new InvalidParameterException(
                        "command '" + cmdName + " requires a value for " + arg.name
                                + ", but there were no remaining arguments after it.");
            } else if (arg.freeform) {
                logger.debug(() -> "freeform parameter:" + nextarg);
            } else if (nextarg.contains("=")) {
                throw new InvalidParameterException(
                        "command '" + cmdName + "' requires a value for " + arg.name +
                                ", but a named parameter was found instead: " + nextarg);
            } else if (CmdType.anyMatches(nextarg)) {
                throw new InvalidParameterException(
                        "command '" + cmdName + "' requires a value for " + arg.name
                                + ", but a reserved word was found instead: " + nextarg);
            }

            logger.debug(() -> "cmd name:" + cmdName + ", positional " + arg.name + ": " + nextarg);
            params.put(arg.name, arg.converter.apply(arglist.removeFirst()).toString());
        }

        while (arglist.size() > 0 &&
                !CmdType.anyMatches(arglist.peekFirst())
                && arglist.peekFirst().contains("=")) {
            String arg = arglist.removeFirst();
            String[] assigned = arg.split("=", 2);
            String pname = assigned[0];
            String pval = assigned[1];


            if (pname.equals("yaml") || pname.equals("workload")) {
                pval = fixer.canonicalizePath(pval);
            }
            if (params.containsKey(pname)) {
                throw new InvalidParameterException("parameter '" + pname + "' is already set for '" + cmdType +"' command. For each command," +
                    " a named parameter may only be set once. Multiple occurrences are disallowed to avoid errors or ambiguity.");
            }
            params.put(pname, pval);
        }

        return new Cmd(cmdType, params);
    }

    public static String toJSONBlock(Map<String, String> map, boolean oneline) {

        int klen = map.keySet().stream().mapToInt(String::length).max().orElse(1);
        StringBuilder sb = new StringBuilder();
        List<String> l = new ArrayList<>();
        for (Map.Entry<String, String> entries : map.entrySet()) {
            String key = entries.getKey();
            String value = removeQuotes(entries.getValue());
            if (oneline) {
                l.add("'" + key + "':'" + value + "'");
            } else {
                l.add("    '" + key + "': " + " ".repeat(klen - key.length()) + "'" + value + "'");
            }
        }
        return "{" + (oneline ? "" : "\n") + String.join(",\n", l) + (oneline ? "}" : "\n}");
    }

    private static String removeQuotes(String value) {
        if (value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;

    }

    public static String toJSONParams(String varname, Map<String, String> map, boolean oneline) {
        return "// params.size==" + map.size() + "\n" + varname + "=" + toJSONBlock(map, oneline);
    }

    public static List<Cmd> parseCmds (String...arglist){
        LinkedList<String> ll = new LinkedList<>(Arrays.asList(arglist));
        List<Cmd> cmds = new ArrayList<>();
        while (!ll.isEmpty()) {
            Cmd cmd = parseArg(ll, null);
            cmds.add(cmd);
        }
        return cmds;
    }

}
