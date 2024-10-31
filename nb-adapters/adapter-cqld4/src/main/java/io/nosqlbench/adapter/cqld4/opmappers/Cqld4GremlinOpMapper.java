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

import com.datastax.oss.driver.api.core.CqlSession;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.opdispensers.Cqld4GremlinOpDispenser;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4ScriptGraphOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class Cqld4GremlinOpMapper<CO extends Cqld4ScriptGraphOp> extends Cqld4BaseOpMapper<Cqld4ScriptGraphOp> {
    private final LongFunction<String> targetFunction;

    public Cqld4GremlinOpMapper(Cqld4DriverAdapter adapter, LongFunction<String> targetFunction) {
        super(adapter);
        this.targetFunction = targetFunction;
    }

    @Override
    public Cqld4GremlinOpDispenser apply(ParsedOp op, LongFunction spaceInitF) {
        return new Cqld4GremlinOpDispenser(adapter, sessionFunc, targetFunction, op);
    }

}
