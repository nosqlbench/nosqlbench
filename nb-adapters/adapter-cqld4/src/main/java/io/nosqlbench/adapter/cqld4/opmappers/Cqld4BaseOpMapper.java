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

package io.nosqlbench.adapter.cqld4.opmappers;

import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4BaseOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public abstract class Cqld4BaseOpMapper<T extends Cqld4BaseOp<?>> implements OpMapper<T,Cqld4Space> {

    protected final static Logger logger = LogManager.getLogger(Cqld4BaseOpMapper.class);
    protected final Cqld4DriverAdapter adapter;

    public Cqld4BaseOpMapper(Cqld4DriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public abstract OpDispenser<T> apply(NBComponent adapterC, ParsedOp op, LongFunction<Cqld4Space> spaceF);
}
