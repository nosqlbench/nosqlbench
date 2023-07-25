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
import io.nosqlbench.adapter.cqld4.optypes.Cqld4RainbowTableOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class CqlD4RainbowTableMapper implements OpMapper<Cqld4RainbowTableOp> {
    private final LongFunction<CqlSession> sessionFunc;
    private final LongFunction<String> targetFunction;
    private final DriverAdapter adapter;

    public CqlD4RainbowTableMapper(DriverAdapter adapter, LongFunction<CqlSession> sessionFunc, LongFunction<String> targetFunction) {
        this.sessionFunc = sessionFunc;
        this.targetFunction = targetFunction;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends Cqld4RainbowTableOp> apply(ParsedOp op) {
        return null;
//        return new CqlD4RainbowTableDispenser(adapter, sessionFunc,targetFunction, op);
    }
}
