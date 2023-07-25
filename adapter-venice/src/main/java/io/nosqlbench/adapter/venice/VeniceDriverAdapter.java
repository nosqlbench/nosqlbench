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

package io.nosqlbench.adapter.venice;

import io.nosqlbench.adapter.venice.ops.VeniceOp;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "venice")
public class VeniceDriverAdapter extends BaseDriverAdapter<VeniceOp, VeniceSpace> {
    private final static Logger logger = LogManager.getLogger(VeniceDriverAdapter.class);

    @Override
    public OpMapper<VeniceOp> getOpMapper() {
        DriverSpaceCache<? extends VeniceSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new VeniceOpMapper(this, adapterConfig, spaceCache);
    }

    @Override
    public Function<String, ? extends VeniceSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new VeniceSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(VeniceSpace.getConfigModel());
    }
}
