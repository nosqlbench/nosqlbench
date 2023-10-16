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

package io.nosqlbench.engine.extensions.optimizers;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBBuilders;
import org.apache.logging.log4j.Logger;

import javax.script.ScriptContext;

public class BobyqaOptimizerPlugin {

    private final Logger logger;
    private final NBBaseComponent baseComponent;
    private final ScriptContext scriptContext;

    public BobyqaOptimizerPlugin(Logger logger, NBBaseComponent baseComponent, ScriptContext scriptContext) {
        this.logger = logger;
        this.baseComponent = baseComponent;
        this.scriptContext = scriptContext;
    }

    public BobyqaOptimizerInstance init() {
        return new BobyqaOptimizerInstance(logger,baseComponent,scriptContext);
    }


}
