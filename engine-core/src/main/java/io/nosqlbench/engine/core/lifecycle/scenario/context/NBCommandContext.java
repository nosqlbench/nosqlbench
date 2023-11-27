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

package io.nosqlbench.engine.core.lifecycle.scenario.context;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.components.NBComponent;
import os.CommandResult;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface NBCommandContext extends NBComponent, BiFunction<NBInvokableCommand,NBCommandParams, NBCommandResult> {
//    ScenarioPhaseParams params();
    ContextActivitiesController controller();
    PrintWriter out();
    PrintWriter err();
    Reader in();
    public static ContextBuilderFacets.WantsName builder() {
        return new NBScenarioContextBuilder();
    }

    default void doShutdown() {
    };
}
