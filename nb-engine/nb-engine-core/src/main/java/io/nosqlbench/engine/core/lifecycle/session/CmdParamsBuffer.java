/*
 * Copyright (c) nosqlbench
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CmdParamsBuffer {
    private final Logger logger = LogManager.getLogger(CmdParamsBuffer.class);
    private List<Cmd> cmds = new ArrayList<>();

    public CmdParamsBuffer() {}
    public CmdParamsBuffer(List<Cmd> cmds) {
        this.cmds.addAll(cmds);
    }

    public void add(Cmd... cmd) {
        this.cmds.addAll(Arrays.asList(cmd));
    }

    public Map<String,String> getGlobalParams() {
        Map<String,String> params = new LinkedHashMap<>();
        for (Cmd cmd : cmds) {
            switch (cmd.getCmdType()) {
                case script:
                case fragment:
                case java:
                    combineGlobalParams(params,cmd);
                    break;
                default:
            }
        }

        return params;
    }

    /**
     * Merge the params from the command into the global params map, but ensure that users know
     * if they are overwriting values, which could cause difficult to find bugs in their scripts.
     *
     * @param scriptParams The existing global params map
     * @param cmd          The command containing the new params to merge in
     */
    private void combineGlobalParams(Map<String, String> scriptParams, Cmd cmd) {
        for (String newkey : cmd.getArgs().keySet()) {
            String newvalue = cmd.getArgs().get(newkey).getValue();

            if (scriptParams.containsKey(newkey)) {
                logger.warn("command '" + cmd.getCmdType() + "' overwrote param '" + newkey + " as " + newvalue);
            }
            scriptParams.put(newkey, newvalue);
        }
    }

}
