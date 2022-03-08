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

package io.nosqlbench.engine.api.extensions;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

/**
 * Any implementation of a SandboxExtension that is found in the runtime
 * can be automatically loaded into the scenario scripting sandbox.
 * The descriptor type is used to flag the object type of the actual
 * instance to be used as the extension point.
 * <p>
 *     Each scenario gets its own instance of an object from this SandboxPlugin
 * </p>
 */
public interface ScriptingPluginInfo<T> {

    /**
     * @return a brief description of this extension.
     */
    String getDescription();

    /**
     * @param logger A logger named for the extension, in case the extension wants to log internally
     * @param metricRegistry The main metric registry, in case the extension wants to track metrics internally
     * @param scriptContext The scripting context object, useful for interacting with the sandbox directly
     * @return a new instance of an extension. The extension is given a logger if it desires.
     */
     T getExtensionObject(Logger logger, MetricRegistry metricRegistry, ScriptContext scriptContext);

    /**
     * @return a simple name at the root of the variable namespace to anchor this extension.
     */
    default String getBaseVariableName() {
        return getClass().getAnnotation(Service.class).selector();
    }

    /**
     * If auto loading is true, then the extension will be injected into every
     * scenario sandbox. If it is false, then the runtime may offer the user the
     * extension via some mechanism.
     * @return whether or not to auto inject this extension into each new scenario
     */
     default boolean isAutoLoading() {
        return true;
    }
}
