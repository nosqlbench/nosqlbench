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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

@Service(value = NBBaseCommand.class, selector = "getenv")
public class CMD_getenv extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("getenv");

    public CMD_getenv(NBBufferedContainer parentComponent, String stepName, String targetScenario) {
        super(parentComponent, stepName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Map<String, String> got = new LinkedHashMap<>();
        params.forEach((k, v) -> {
            String value = System.getenv(v);
            if (value == null) {
                logger.warn(() -> "tried to get env var with name '" + v + "', but it was not defined");
            } else {
                got.put(k,value);
            }
        });

        return got;
    }
}
