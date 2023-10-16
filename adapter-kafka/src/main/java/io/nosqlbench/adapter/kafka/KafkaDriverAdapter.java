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

package io.nosqlbench.adapter.kafka;

import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;

@Service(value = DriverAdapter.class, selector = "kafka")
public class KafkaDriverAdapter extends BaseDriverAdapter<KafkaOp, KafkaSpace> {
    private final static Logger logger = LogManager.getLogger(KafkaDriverAdapter.class);

    public KafkaDriverAdapter(NBComponent parentComponent) {
        super(parentComponent);
    }

    @Override
    public OpMapper<KafkaOp> getOpMapper() {
        DriverSpaceCache<? extends KafkaSpace> spaceCache = getSpaceCache();
        NBConfiguration adapterConfig = getConfiguration();
        return new KafkaOpMapper(this, adapterConfig, spaceCache);
    }

    @Override
    public Function<String, ? extends KafkaSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new KafkaSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(KafkaSpace.getConfigModel());
    }
}
