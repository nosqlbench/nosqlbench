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

package io.nosqlbench.engine.core.lifecycle.scenario.direct;

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioPhaseParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenarioPhase;

import java.io.PrintWriter;
import java.io.Reader;

public abstract class SCBaseScenarioPhase extends NBScenarioPhase {
    protected Reader stdin;
    protected PrintWriter stdout;
    protected PrintWriter stderr;
    protected ScenarioActivitiesController controller;

    public SCBaseScenarioPhase(NBComponent parentComponent, String phaseName, String targetScenario) {
        super(parentComponent, phaseName, targetScenario);
    }
    public SCBaseScenarioPhase(NBComponent parentComponent, String phaseName) {
        super(parentComponent, phaseName, "default");
    }


    @Override
    protected final void runScenarioPhase(NBScenarioContext shell, ScenarioPhaseParams params) {
        this.stdin = shell.in();
        this.stdout = shell.out();
        this.stderr = shell.err();
        this.controller = shell.controller();
        try {
            invoke(params);
        } catch (Exception e) {
            stdout.println(e.toString());
            throw e;
        }
    }

    /**
     * Subclasses must implement this method, which emulates the scope
     * of previous scenario scripts. Within this method, local
     * fields will be available directly:
     * <UL>
     *     <LI>component, an {@link NBComponent} - The NB component upon which all metrics or other services are attached.</LI>
     *     <LI>stdin - a {@link Reader} representing the input buffer which would normally be {@link System#in}
     *     <LI>stdout, stderr</LI>- a {@link PrintWriter}; This can be buffered virtually, attached to {@link System#out} and {@link System#err} or both for IO tracing.</LI>
     *     <LI>controller - A dedicated {@link ScenarioActivitiesController} which can be used to define, start, top, and interact with activities.</LI>
     *     <LI>params - The {@link ScenarioPhaseParams} which have been passed to this scenario.</LI>
     *     <LI><EM>all component services</EM> as this scenario IS a component. This includes all implemented methods in any of the {@link NBComponent} sub-interfaces.</EM>
     *     </LI>
     * </UL>
     */
    public abstract void invoke(ScenarioPhaseParams params);

}
