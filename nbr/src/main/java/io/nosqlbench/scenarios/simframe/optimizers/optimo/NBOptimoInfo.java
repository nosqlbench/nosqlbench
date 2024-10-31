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

package io.nosqlbench.scenarios.simframe.optimizers.optimo;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandInfo;
import io.nosqlbench.nb.annotations.Service;

@Service(value = NBCommandInfo.class,selector = "optimo")
public class NBOptimoInfo extends NBCommandInfo {
    @Override
    public Class<? extends NBBaseCommand> getType() {
        return CMD_optimo.class;
    }

    @Override
    public String getHelp() {
        return """
            (experimental) invoke the optimo multi-variate optimizer on a running activity
            """;
    }

}
