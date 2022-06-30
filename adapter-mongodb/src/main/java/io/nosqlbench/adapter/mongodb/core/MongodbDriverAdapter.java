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

package io.nosqlbench.adapter.mongodb.core;

import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.BaseDriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.function.Function;

/**
 * Special thanks to Justin Chu who authored the original NoSQLBench MongoDB ActivityType.
 */
@Service(value=DriverAdapter.class, selector ="mongodb")
public class MongodbDriverAdapter extends BaseDriverAdapter<Op, MongoSpace> {

    @Override
    public OpMapper<Op> getOpMapper() {
        return new MongodbOpMapper(this, getSpaceCache());
    }

    @Override
    public Function<String, ? extends MongoSpace> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new MongoSpace(s, cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(MongoSpace.getConfigModel());
    }
}
