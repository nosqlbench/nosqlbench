/*
 * Copyright (c) 2022-2023 nosqlbench
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
package io.nosqlbench.engine.core.lifecycle.scenario.script;

import io.nosqlbench.api.config.LabeledScenarioContext;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenarioController;

import java.util.Map;

public class ScenarioContext extends ScriptEnvBuffer implements LabeledScenarioContext {

    private final ScenarioController sc;
    private final String contextName;

    public ScenarioContext(final String contextName, final ScenarioController sc) {
        this.contextName = contextName;
        this.sc = sc;
    }

    public String getContextName() {
        return contextName;
    }

    @Override
    public Object getAttribute(final String name) {
        final Object o = super.getAttribute(name);
        return o;
    }

    @Override
    public Object getAttribute(final String name, final int scope) {
        final Object o = super.getAttribute(name, scope);
        return o;
    }

    @Override
    public void setAttribute(final String name, final Object value, final int scope) {
        super.setAttribute(name, value, scope);
    }

    @Override
    public Map<String, String> getLabels() {
        return Map.of("scenario", contextName);
    }
}
