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

package io.nosqlbench.nbr.examples;

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScriptParams;
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
    protected ScriptParams params;

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
        invoke();
    }

    public abstract void invoke();

}
