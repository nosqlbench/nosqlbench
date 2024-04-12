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

package io.nosqlbench.adapters.api.evalctx;

import groovy.lang.Binding;
import io.nosqlbench.virtdata.core.templates.ParsedTemplateString;

import java.util.List;

public class GroovyBooleanCycleFunction extends GroovyCycleFunction<Boolean> {

    public GroovyBooleanCycleFunction(String name, ParsedTemplateString template, List<String> imports, List<Class<?>> staticSymbolImports, Binding binding) {
        super(name, template, imports, staticSymbolImports, binding);
    }

    @Override
    public Boolean apply(long value) {
        return super.apply(value);
    }

}
