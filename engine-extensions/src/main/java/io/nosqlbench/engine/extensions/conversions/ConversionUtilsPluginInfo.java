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

package io.nosqlbench.engine.extensions.conversions;

import com.codahale.metrics.MetricRegistry;
import io.nosqlbench.api.config.LabeledScenarioContext;
import io.nosqlbench.api.engine.metrics.MetricsRegistry;
import io.nosqlbench.api.extensions.ScriptingExtensionPluginInfo;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Service(value = ScriptingExtensionPluginInfo.class,selector = "convert")
public class ConversionUtilsPluginInfo implements ScriptingExtensionPluginInfo<ConverterUtils> {
    @Override
    public String getDescription() {
        return "Utilities to convert between common basic data types";
    }

    @Override
    public ConverterUtils getExtensionObject(Logger logger, MetricsRegistry metricRegistry, LabeledScenarioContext scriptContext) {
        return new ConverterUtils();
    }

    @Override
    public List<Class<?>> autoImportStaticMethodClasses() {
        return List.of(ConverterUtils.class);
    }
}
