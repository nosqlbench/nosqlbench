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

package io.nosqlbench.engine.cmdstream;

import io.nosqlbench.nb.api.errors.BasicError;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.*;

/**
 * Encapsulate Command parsing and structure for the NoSQLBench command line. Commands always have a name, sometimes
 * have a list of positional arguments, and sometimes have a map of named parameters. An example of a command tha thas
 * both would look like {@code script test.js p1=v1}
 */
public class Cmd {

    private final static Logger logger = LogManager.getLogger(Cmd.class);
    public static final String DEFAULT_TARGET_CONTEXT = "default";
    private final String targetContextName;

    private final Map<String, CmdArg> cmdArgs;
    private final String stepName;


    public String getTargetContext() {
        return targetContextName;
    }

    public Cmd forContainer(String containerName, String stepName) {
        return new Cmd(cmdType, cmdArgs, containerName, stepName);
    }


    public String getArgValueOrNull(String paramName) {
        CmdArg cmdArg = this.cmdArgs.get(paramName);
        if (cmdArg==null) {
            return null;
        }
        return cmdArg.getValue();

    }

    public String takeArgValue(String paramName) {
        String argValue = getArgValue(paramName);
        this.cmdArgs.remove(paramName);
        return argValue;
    }
    public String getArgValue(String paramName) {
        CmdArg cmdArg = this.cmdArgs.get(paramName);
        if (cmdArg==null) {
            throw new BasicError("Could not get param value for undefined arg '" + paramName + "'");
        }
        return cmdArg.getValue();
    }

    private final CmdType cmdType;

//    public Cmd(@NotNull CmdType cmdType, Map<String, CmdArg> cmdArgs, String targetContextName, String stepName) {
//        this.cmdArgs = cmdArgs;
//        this.cmdType = cmdType;
//        this.targetContextName = targetContextName;
//    }

//    public Cmd(String indirect, Map<String,String> args) {
//
//    }
//
//    public Cmd(String cmdType, CmdArg... args) {
//
//    }
//
    public Cmd(@NotNull String cmdTypeOrName, Map<String, CmdArg> argmap) {
        this.cmdType = CmdType.valueOfAnyCaseOrIndirect(cmdTypeOrName);
        this.targetContextName = DEFAULT_TARGET_CONTEXT;
        this.stepName = "";
        this.cmdArgs = new LinkedHashMap<>();
        if (this.cmdType == CmdType.indirect) {
            this.cmdArgs.put("_impl", new CmdArg(new CmdParam("_impl", s->s, false),"===",cmdTypeOrName));
        }
        cmdArgs.putAll(argmap);
//        if (!basket.isEmpty()) {
//            throw new BasicError("extraneous arguments provided for " + this + ": " + basket);
//        }
    }


    public Cmd(@NotNull CmdType cmdType, Map<String, CmdArg> cmdArgMap) {
        this.cmdType = cmdType;
        this.stepName="NO-STEP";
        this.targetContextName = DEFAULT_TARGET_CONTEXT;
        this.cmdArgs = new LinkedHashMap<>();
        if (cmdType == CmdType.indirect && !cmdArgMap.containsKey("_impl")) {
            throw new RuntimeException("indirect cmd type is invalid without a '_impl' parameter.");
        }
        cmdArgs.putAll(cmdArgMap);
    }

    public Cmd(@NotNull CmdType cmdType, Map<String, CmdArg> cmdArgMap, String targetContext, String stepName) {
        this.cmdType = cmdType;
        this.stepName=stepName;
        this.targetContextName = targetContext;
        this.cmdArgs = new LinkedHashMap<>();
        if (cmdType == CmdType.indirect && !cmdArgMap.containsKey("_impl")) {
            throw new RuntimeException("indirect cmd type is invalid without a '_impl' parameter.");
        }
        this.cmdArgs.putAll(cmdArgMap);
    }

    public CmdType getCmdType() {
        return cmdType;
    }

    // TODO: Since this replaced an implicit String, the types need to be disambiguated
    public Map<String, CmdArg> getArgs() {
        return cmdArgs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdType.toString());
        for (CmdArg value : getArgs().values()) {
            sb.append(" ").append(value);
        }
        return sb.toString();
    }

    public String asScriptText() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCmdType().toString());
        sb.append("(");
        if (this.cmdArgs.size() > getCmdType().getPositionalArgs().length) {
            sb.append(toJSONBlock(getArgMap(), false));
        } else {
            for (String value : getArgMap().values()) {
                String trimmed = ((value.startsWith("'") && value.endsWith("'"))
                    || (value.startsWith("\"")) && value.endsWith("\"")) ?
                    value.substring(1, value.length() - 1) : value;
                sb.append("'").append(trimmed).append("'").append(",");
            }
            sb.setLength(sb.length() - 1);
        }
        sb.append(");");
        return sb.toString();
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

    public Map<String, String> getArgMap() {
        LinkedHashMap<String, String> argmap = new LinkedHashMap<>();
        this.cmdArgs.forEach((k,v) -> {
            argmap.put(k,v.getValue());
        });
        return argmap;
    }

    public String getStepName() {
        return this.stepName;
    }

//    public static List<Cmd> parseCmds(String... arglist) {
//        LinkedList<String> ll = new LinkedList<>(Arrays.asList(arglist));
//        List<Cmd> cmds = new ArrayList<>();
//        while (!ll.isEmpty()) {
//            Cmd cmd = parseArg(ll, null);
//            cmds.add(cmd);
//        }
//        return cmds;
//    }

}
