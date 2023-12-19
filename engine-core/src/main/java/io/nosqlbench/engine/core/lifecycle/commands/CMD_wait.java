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

import com.amazonaws.services.s3.model.transform.Unmarshallers;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Optional;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.LongStream;

@Service(value = NBBaseCommand.class, selector = "example")
public class CMD_wait extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("example");

    public CMD_wait(NBBufferedContainer parentComponent, String stepName, String targetScenario) {
        super(parentComponent, stepName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        long ns = 0L;
        ns += params.maybeGet("ms")
            .or(() -> params.maybeGet("millis"))
            .map(Long::parseLong)
            .map(l -> l * 1_000_000L)
            .orElse(0L);
        ns += params.maybeGet("us")
            .or(() -> params.maybeGet("micros"))
            .map(Long::parseLong)
            .map(l -> l * 1_000L)
            .orElse(0L);
        ns += params.maybeGet("ns")
            .or(() -> params.maybeGet("nanos"))
            .map(Long::parseLong)
            .orElse(0L);
        ns += params.maybeGet("unit")
                .flatMap(Unit::nanosecondsFor)
                .orElse(0L);
        LockSupport.parkNanos(ns);
        return ns;
    }
}
