/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpGenerator;
import io.nosqlbench.adapters.api.evalctx.CycleFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Predicate;

public class PollingOp<T> implements CycleOp<T>, OpGenerator {
    private final static Logger logger = LogManager.getLogger(PollingOp.class);

    private final CycleOp<T> innerOp;
    private final CycleFunction<Boolean> untilCondition;
    private PollingOp<T> nextOp = null;

    public PollingOp(CycleOp<T> innerOp, CycleFunction<Boolean> untilCondition) {
        this.innerOp = innerOp;
        this.untilCondition = untilCondition;
    }
    @Override
    public synchronized T apply(long value) {
        T result = this.innerOp.apply(value);
        untilCondition.setVariable("result",result);
        boolean conditionIsMet = untilCondition.apply(value);
        if (conditionIsMet) {
            onConditionMet(result);
            this.nextOp=null;
        } else {
            this.nextOp=this;
            onConditionUnmet(result);
        }
        return result;
    }

    @Override
    public synchronized Op getNextOp() {
        return nextOp;
    }

    protected void onConditionMet(T value) {
        logger.debug("for op " + this + ": condition MET for result " + value);
    }

    protected void onConditionUnmet(T value) {
        logger.debug("for op " + this + ": condition UNMET for result " + value);
    }
}
