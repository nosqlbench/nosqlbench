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

package io.nosqlbench.engine.core.lifecycle.scenario.execution;

import io.nosqlbench.nb.api.annotations.Annotation;
import io.nosqlbench.nb.api.annotations.Layer;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.nb.api.components.NBComponentErrorHandler;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;

public abstract class NBBaseCommand extends NBInvokableCommand {

    private final String targetScenario;
    protected Logger logger = LogManager.getLogger("COMMAND");

    public NBBaseCommand(NBBufferedCommandContext parentComponent, String phaseName, String targetScenario) {
        super(parentComponent, NBLabels.forKV("command", phaseName));
        this.targetScenario = targetScenario;
    }

    public NBBaseCommand(NBBufferedCommandContext parentComponent, String commandLabel) {
        this(parentComponent, commandLabel, "_testing_");
    }

    public String getScenarioName() {
        return getLabels().asMap().get("scenario");
    }

    public String getTargetScenario() {
        return this.targetScenario;
    }

    @Override
    public final Object apply(NBBufferedCommandContext sctx, NBCommandParams params) {
        return invoke(params, sctx.out(), sctx.err(), sctx.in(), sctx.controller());
    }

    @Override
    public String toString() {
        return "SCENARIO (" + this.getClass().getSuperclass().getSimpleName() + ") { scenarioName: " + getScenarioName() + " }";
    }

    public abstract Object invoke(
        NBCommandParams params,
        PrintWriter stdout,
        PrintWriter stderr,
        Reader stdin,
        ContextActivitiesController controller
    );


}
