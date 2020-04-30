/*
 *
 *    Copyright 2016 jshook
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package io.nosqlbench.engine.extensions.example;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import org.slf4j.Logger;

import javax.script.ScriptContext;

@Service(ScriptingPluginInfo.class)
public class ExamplePluginData implements ScriptingPluginInfo<ExamplePlugin> {

    @Override
    public String getDescription() {
        return "This is an example of a dynamically loadable script extension. It just adds two ints when" +
                "you call the getSum(...) method.";
    }

    @Override
    public ExamplePlugin getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext) {
        logger.trace("creating a new ExampleSandboxExtension");
        return new ExamplePlugin();
    }

    @Override
    public String getBaseVariableName() {
        return "adder";
    }

}
