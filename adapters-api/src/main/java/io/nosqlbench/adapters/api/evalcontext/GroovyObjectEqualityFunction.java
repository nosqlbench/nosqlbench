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

package io.nosqlbench.adapters.api.evalcontext;

import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;

import java.util.List;
import java.util.Map;

/**
 * This evaluator uses {@link Object#equals(Object)} to compare the results of an operation with
 * a constructed value. The script used is meant to only construct the object to compare with.
 * All context variables can be injected into the script context except for one, the <em>result</em>
 * variable. This is intercepted and then used as a basis for comparison to the result of executing the
 * script.
 */
public class GroovyObjectEqualityFunction extends GroovyCycleFunction<Boolean> {

    private Object result;

    public GroovyObjectEqualityFunction(String name, ParsedTemplateString template, List<String> imports) {
        super(name, template, imports);
    }

    @Override
    public Boolean apply(long value) {
        Map<String, Object> values = bindingFunctions.getAllMap(value);
        values.forEach((k,v)-> variableBindings.setVariable(k,v));
        Object scriptResult= script.run();
        return scriptResult.equals(result);
    }

    /**
     * Intercept and reserve the value of the result injected variable for comparison to the evaluated script result later.
     */
    public void setVariable(String name, Object value) {
        if (name.equals("result")) {
            this.result = value;
            return;
        }

        super.setVariable(name, value);
    }
}
