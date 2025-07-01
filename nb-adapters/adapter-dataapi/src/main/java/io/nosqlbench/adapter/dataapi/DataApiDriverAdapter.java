/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.dataapi;

import io.nosqlbench.adapter.dataapi.ops.DataApiBaseOp;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.labels.NBLabels;

import java.util.function.Function;
import java.util.function.LongFunction;

// TODO: Add details to dataapi.md in main resources folder, a la cqld4.md

@Service(value = DriverAdapter.class, selector = "dataapi")
public class DataApiDriverAdapter extends BaseDriverAdapter<DataApiBaseOp, DataApiSpace> {
    public DataApiDriverAdapter(NBComponent parent, NBLabels childLabels) {
        super(parent, childLabels);
    }

    @Override
    public OpMapper getOpMapper() {
        return new DataApiOpMapper(this);
    }

    @Override
    public Function<String, DataApiSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (name) -> new DataApiSpace(this, Long.parseLong(name), cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(DataApiSpace.getConfigModel());
    }
}
