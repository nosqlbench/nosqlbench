/*
*   Copyright 2016 jshook
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/
package io.nosqlbench.script;

import io.nosqlbench.core.ScenarioController;
import io.nosqlbench.scripting.ScriptEnvBuffer;

public class ScenarioContext extends ScriptEnvBuffer {

    private ScenarioController sc;

    public ScenarioContext(ScenarioController sc) {
        this.sc = sc;
    }

    @Override
    public Object getAttribute(String name) {
        Object o = super.getAttribute(name);
        return o;
    }

    @Override
    public Object getAttribute(String name, int scope) {
        Object o = super.getAttribute(name, scope);
        return o;
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        super.setAttribute(name, value, scope);
    }

}
