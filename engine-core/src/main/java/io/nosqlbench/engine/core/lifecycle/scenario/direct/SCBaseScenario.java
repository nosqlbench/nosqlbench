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
import io.nosqlbench.engine.core.lifecycle.scenario.context.ActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.Extensions;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

public abstract class SCBaseScenario extends NBScenario {
    protected NBComponent component;
    protected Reader stdin;
    protected PrintWriter stdout;
    protected Writer stderr;
    protected ActivitiesController controller;
    protected ScenarioParams params;
    protected Extensions extensions;

    public SCBaseScenario(NBComponent parentComponent, String scenarioName) {
        super(parentComponent, scenarioName);
    }

    @Override
    protected final void runScenario(NBSceneFixtures shell) {
        this.component = shell.component();
        this.stdin = shell.in();
        this.stdout = shell.out();
        this.stderr = shell.err();
        this.controller = shell.controller();
        this.params = shell.params();
        this.extensions = shell.extensions();
        invoke();
    }

    /**
     * Subclasses must implement this method, which emulates the scope
     * of previous scenario scripts. Within this method, local
     * fields will be available directly:
     * <UL>
     *     <LI>component, an {@link NBComponent} - The NB component upon which all metrics or other services are attached.</LI>
     *     <LI>stdin - a {@link Reader} representing the input buffer which would normally be {@link System#in}
     *     <LI>stdout, stderr</LI>- a {@link PrintWriter}; This can be buffered virtually, attached to {@link System#out} and {@link System#err} or both for IO tracing.</LI>
     *     <LI>controller - A dedicated {@link ActivitiesController} which can be used to define, start, top, and interact with activities.</LI>
     *     <LI>params - The {@link ScenarioParams} which have been passed to this scenario.</LI>
     *     <LI>extensions - A dedicated ahndle to the {@link Extensions} service.</LI>
     *     <LI><EM>all component services</EM> as this scenario IS a component. This includes all implemented methods in any of the {@link NBComponent} sub-interfaces.</EM>
     *     </LI>
     * </UL>
     */
    public abstract void invoke();

}
