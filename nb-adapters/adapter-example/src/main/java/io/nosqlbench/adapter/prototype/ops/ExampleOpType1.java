package io.nosqlbench.adapter.prototype.ops;

/*
 * Copyright (c) nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.adapter.prototype.results.ExampleStringResult;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExampleOpType1 implements CycleOp<ExampleStringResult> {

    private final static Logger logger = LogManager.getLogger(ExampleOpType1.class);
    private final long cycle;

    public ExampleOpType1(long cycle) {
        this.cycle = cycle;
    }

    @Override
    public ExampleStringResult apply(long value) {
        return new ExampleStringResult("ProtoOpType1 cycle(" + value + ")");
    }
}
