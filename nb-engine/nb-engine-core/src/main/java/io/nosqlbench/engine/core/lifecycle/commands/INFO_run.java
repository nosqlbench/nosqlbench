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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandInfo;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.annotations.Service;

@Service(value = NBCommandInfo.class,selector = "run")
public class INFO_run extends NBCommandInfo {
    @Override
    public Class<? extends NBInvokableCommand> getType() {
        return CMD_run.class;
    }

    @Override
    public String getHelp() {
        return """
            run an activity, blocking the main control thread until it is complete

            Thorough documentation for these options can be found at:
            https://docs.nosqlbench.io/user-guide/core-activity-params/

            Essential parameters for this command are:
            * driver       (see --list-drivers to see what your runtime has built-in)
            * workload     a workload template in yaml, JSON, or Jsonnet form
            * tags         a set of filtering tags to enable or disable specific ops
            * threads      the number of concurrent requests to run
            * cycles       the total number of operations to run
            * errors       the error handling rules

            Diagnostic Options
            * dryrun       enable dryrun diagnostics at different levels

            Metrics Options
            * alias        name the activity so that you can observer or modify it concurrently
            * instrument   enable per-op-template metrics collection and reporting
            * hdr_digits   set the number of significant digits in histogram collection

            Customization
            * cyclerate    set the ops/s for the activity
            * rate         synonym for cyclerate
            * stride       override the internal micro-batching step size (careful here)
            * striderate   set the rate for strides / second
            * seq          set the op sequencer to use
            """;
    }
}
