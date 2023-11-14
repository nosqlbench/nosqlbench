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

package io.nosqlbench.engine.extensions.computefunctions;

import io.nosqlbench.nb.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Service(value = ScriptingExtensionPluginInfo.class,selector = "compute")
public class ComputeFunctionPluginInfo implements ScriptingExtensionPluginInfo<ComputeFunctions> {
    @Override
    public String getDescription() {
        return "Various small math utilities.";
    }

    @Override
    public ComputeFunctions getExtensionObject(Logger logger, NBComponent baseComponent) {
        return new ComputeFunctions(baseComponent);
    }

    @Override
    public List<Class<?>> autoImportStaticMethodClasses() {
        return List.of(ComputeFunctions.class, RelevancyFunctions.class);
    }
}
