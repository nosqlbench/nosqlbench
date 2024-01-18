/*
 * Copyright (c) 2024 nosqlbench
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
import scala.concurrent.impl.FutureConvertersImpl;

public class UnresolvedCommand extends RuntimeException {
    private final Cmd cmd;

    public UnresolvedCommand(Cmd cmd, Throwable cause) {
        super(cause);
        this.cmd = cmd;
    }

    public String toString() {
        final String helpmsg = """
                Could not recognize command 'ARG'.
                This means that all of the following searches for a compatible command failed:
                1. commands: no scenario command named 'ARG' is known. (start, run, await, ...)
                2. scripts: no auto script named './scripts/auto/ARG.js' in the local filesystem.
                3. scripts: no auto script named 'scripts/auto/ARG.js' was found in the PROG binary.
                4. workloads: no workload file named ARG[.yaml] was found in the local filesystem, even in include paths INCLUDES.
                5. workloads: no workload file named ARG[.yaml] was bundled in PROG binary, even in include paths INCLUDES.
                6. apps: no application named ARG was bundled in PROG.

                You can discover available ways to invoke PROG by using the various --list-* commands:
                [ --list-commands, --list-scripts, --list-workloads (and --list-scenarios), --list-apps ]
                """
            .replaceAll("ARG", getCmdName())
            .replaceAll("PROG", "nb5");

        return super.toString() + "\nadditional diagnostics:\n" + helpmsg;
    }

    private String getCmdName() {
        String impl = cmd.getArgMap().get("_impl");
        if (impl!=null) return impl;
        return cmd.toString();
    }

}
