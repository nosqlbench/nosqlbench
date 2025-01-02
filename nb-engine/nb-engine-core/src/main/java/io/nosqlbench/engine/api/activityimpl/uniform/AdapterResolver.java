package io.nosqlbench.engine.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigurable;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.ServiceLoader;
import java.util.function.BiFunction;

public class AdapterResolver
    //    implements BiFunction<String, NBConfiguration, DriverAdapter<? extends CycleOp<?>, Space>>
{
    public DriverAdapter<? extends CycleOp<?>, Space> apply(
        NBComponent parent,
        String name,
        NBConfiguration configSuperset
    )
    {
        ServiceSelector<DriverAdapterLoader>
            loader =
            ServiceSelector.of(name, ServiceLoader.load(DriverAdapterLoader.class));
        DriverAdapterLoader
            dal =
            loader.get()
                .orElseThrow(() -> new OpConfigError("No DriverAdapterLoader found for " + name));
        DriverAdapter<CycleOp<?>, Space> adapter = dal.load(parent, NBLabels.forKV());

        if (adapter instanceof NBConfigurable configurable) {
            NBConfigModel adapterModel = configurable.getConfigModel();
            NBConfiguration matchingConfig = adapterModel.matchConfig(configSuperset.getMap());
            configurable.applyConfig(matchingConfig);
        }

        return adapter;
    }
}
