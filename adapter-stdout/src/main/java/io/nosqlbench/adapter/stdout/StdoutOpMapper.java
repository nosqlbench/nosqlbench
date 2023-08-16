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

package io.nosqlbench.adapter.stdout;

import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class StdoutOpMapper implements OpMapper<StdoutOp> {

    private final DriverSpaceCache<? extends StdoutSpace> ctxcache;
    private final DriverAdapter adapter;

    public StdoutOpMapper(DriverAdapter adapter, DriverSpaceCache<? extends StdoutSpace> ctxcache) {
        this.ctxcache = ctxcache;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<StdoutOp> apply(ParsedOp op) {
        LongFunction<String> spacefunc = op.getAsFunctionOr("space", "default");
        LongFunction<StdoutSpace> ctxfunc = (cycle) -> ctxcache.get(spacefunc.apply(cycle));
        return new StdoutOpDispenser(adapter,op,ctxfunc);
    }

}
