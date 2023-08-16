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

package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.pinecone.ops.PineconeOp;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "pinecone")
public class PineconeDriverAdapter extends BaseDriverAdapter<PineconeOp, PineconeSpace> {

    @Override
    public OpMapper<PineconeOp> getOpMapper() {
        DriverSpaceCache<? extends PineconeSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new PineconeOpMapper(this, spaceCache, adapterConfig);
    }

    @Override
    public Function<String, ? extends PineconeSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new PineconeSpace(s,cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(PineconeSpace.getConfigModel());
    }

}
