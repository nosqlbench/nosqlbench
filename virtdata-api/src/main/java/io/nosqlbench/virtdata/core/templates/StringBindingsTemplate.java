/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.core.templates;

//import io.nosqlbench.engine.api.activityconfig.ParsedStmt;
//import io.nosqlbench.engine.api.activityconfig.yaml.StmtDef;

import io.nosqlbench.virtdata.core.bindings.BindingsTemplate;

/**
 * Uses a string template and a bindings template to create instances of {@link StringBindings}.
 */
public class StringBindingsTemplate {

    private final String stringTemplate;
    private final BindingsTemplate bindingsTemplate;

    public StringBindingsTemplate(String stringTemplate, BindingsTemplate bindingsTemplate) {
        this.stringTemplate = stringTemplate;
        this.bindingsTemplate = bindingsTemplate;
    }

    /**
     * Create a new instance of {@link StringBindings}, preferably in the thread context that will use it.
     * @return a new StringBindings
     */
    public StringBindings resolve() {
        return new StringBindings(stringTemplate,bindingsTemplate);
    }

    @Override
    public String toString() {
        return "TEMPLATE:"+this.stringTemplate+" BINDING:"+bindingsTemplate.toString();
    }
}
