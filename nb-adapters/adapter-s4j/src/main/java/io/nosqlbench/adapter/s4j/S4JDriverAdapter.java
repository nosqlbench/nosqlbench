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

package io.nosqlbench.adapter.s4j;

import io.nosqlbench.adapter.s4j.ops.S4JOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.ConcurrentSpaceCache;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.annotations.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.IntFunction;
import java.util.function.LongFunction;

@Service(value = DriverAdapter.class, selector = "s4j")
public class S4JDriverAdapter extends BaseDriverAdapter<S4JOp, S4JSpace> {
    private final static Logger logger = LogManager.getLogger(S4JDriverAdapter.class);

    public S4JDriverAdapter(NBComponent parentComponent, NBLabels labels) {
        super(parentComponent, labels);
    }

    @Override
    public OpMapper<S4JOp,S4JSpace> getOpMapper() {
        return new S4JOpMapper(this);
    }

    @Override
    public LongFunction<S4JSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (s) -> new S4JSpace(this,s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(S4JSpace.getConfigModel());
    }

}
