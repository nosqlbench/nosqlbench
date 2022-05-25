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

package io.nosqlbench.adapter.jmx;

import io.nosqlbench.adapter.jmx.mappers.JMXOpMapper;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigurable;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

@Service(value = DriverAdapter.class, selector = "jmx-v2")
public class JMXDriverAdapter implements DriverAdapter<Op,JMXSpace>, NBConfigurable {

    private NBConfiguration config;

    @Override
    public OpMapper<Op> getOpMapper() {
        return new JMXOpMapper(getSpaceCache());
    }

    @Override
    public DriverSpaceCache<? extends JMXSpace> getSpaceCache() {
        return new DriverSpaceCache<>(JMXSpace::new);
    }

    @Override
    public NBConfiguration getConfiguration() {
        return config;
    }

    @Override
    public void applyConfig(NBConfiguration cfg) {
        this.config = cfg;
    }

    @Override
    public NBConfigModel getConfigModel() {
        return new JMXSpace("test").getConfigModel();
    }
}
