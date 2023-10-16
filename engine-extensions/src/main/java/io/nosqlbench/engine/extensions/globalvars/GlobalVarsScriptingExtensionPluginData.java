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

package io.nosqlbench.engine.extensions.globalvars;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.config.LabeledScenarioContext;
import io.nosqlbench.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;

@Service(value = ScriptingExtensionPluginInfo.class, selector = "globalvars")
public class GlobalVarsScriptingExtensionPluginData implements ScriptingExtensionPluginInfo<ConcurrentHashMap<String, Object>> {

    @Override
    public String getDescription() {
        return "The global access map from shared state";
    }

    @Override
    public ConcurrentHashMap<String, Object> getExtensionObject(final Logger logger, final NBBaseComponent baseComponent, final LabeledScenarioContext scriptContext) {
        final ConcurrentHashMap<String, Object> map = SharedState.gl_ObjectMap;
        return map;
    }

}
