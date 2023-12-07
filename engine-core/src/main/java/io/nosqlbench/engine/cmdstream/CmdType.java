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

package io.nosqlbench.engine.cmdstream;

/**
 * Command verbs define the names and possible parameters for valid commands.
 * One special type known as 'indirect' encodes verbs which are not directly mapped to specific
 * command implementations. This type is resolved based on the available commands in the runtime,
 * or an error is thrown.
 */
public enum CmdType {
    run(),
    start(),
    stop(CmdParam.of("activity")),
    forceStop(CmdParam.of("activity")),
    script(CmdParam.of("path", s -> s)),
    java(CmdParam.of("class", s -> s)),
    await(CmdParam.of("activity")),
    waitMillis(CmdParam.of("ms", Long::parseLong)),
    fragment(CmdParam.ofFreeform("fragment")),
    container(CmdParam.of("name")),
    indirect(CmdParam.of("indirect"));

    private final CmdParam<?>[] positional;

    CmdType(CmdParam<?>... positional) {
        this.positional = positional;
    }

    public static boolean anyMatches(String s) {
        CmdType found = valueOfAnyCaseOrIndirect(s);
        return found != null;
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

    public CmdParam<?>[] getPositionalArgs() {
        return positional;
    }

    public CmdParam<?> getNamedParam(String varname) {
        for (CmdParam<?> cmdParam : positional) {
            if (cmdParam.name.equals(varname)) {
                return cmdParam;
            }
        }
        return new CmdParam<Object>(varname,s -> s, false);
    }
}
