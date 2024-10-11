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

package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.pulsar.ops.PulsarOp;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.StringDriverSpaceCache;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "pulsar")
public class PulsarDriverAdapter extends BaseDriverAdapter<PulsarOp, PulsarSpace> {

    private final static Logger logger = LogManager.getLogger(PulsarDriverAdapter.class);

    public PulsarDriverAdapter(NBComponent parentComponent, NBLabels labels) {
        super(parentComponent,labels);
    }

    @Override
    public OpMapper<PulsarOp> getOpMapper() {
        StringDriverSpaceCache<? extends PulsarSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new PulsarOpMapper(this, adapterConfig, spaceCache);
    }

    @Override
    public Function<String, ? extends PulsarSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new PulsarSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(PulsarSpace.getConfigModel());
    }

}
