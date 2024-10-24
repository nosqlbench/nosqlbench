package io.nosqlbench.engine.api.activityimpl;

/*
 * Copyright (c) 2022 nosqlbench
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


import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.RunnableOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.DryCycleOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.DryRunnableOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.EmitterCycleOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.EmitterRunnableOpDispenserWrapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpWrappers {

    public final static Logger logger = LogManager.getLogger(OpWrappers.class);

    public static <OP extends Op, SPACE extends Space> OpDispenser<OP> wrapOptionally(
        DriverAdapter<OP, SPACE> adapter,
        OpDispenser<OP> dispenser,
        ParsedOp pop,
        String dryrunSpec
    ) {
        if (dryrunSpec.isEmpty() || "none".equals(dryrunSpec)) {
            return dispenser;
        }


        if ("op".equalsIgnoreCase(dryrunSpec)) {
            Op exampleOp = dispenser.getOp(0L);

            if (exampleOp instanceof RunnableOp runnableOp) {
                dispenser = new DryRunnableOpDispenserWrapper(adapter, pop, dispenser);
            } else if (exampleOp instanceof CycleOp<?> cycleOp) {
                dispenser = new DryCycleOpDispenserWrapper(adapter, pop, dispenser);
            } else {
                throw new OpConfigError("Unable to wrap op named '" + pop.getDefinedNames() + "' for dry run, since" +
                    "only RunnableOp and CycleOp<Result> types are supported");
            }
            logger.warn(
                "initialized {} for dry run only. " +
                    "This op will be synthesized for each cycle, but will not be executed.",
                pop.getName()
            );

        } else if ("emit".equalsIgnoreCase(dryrunSpec)) {
            Op exampleOp = dispenser.getOp(0L);
            if (exampleOp instanceof RunnableOp runnableOp) {
                dispenser = new EmitterRunnableOpDispenserWrapper(adapter, pop, dispenser);
            } else if (exampleOp instanceof CycleOp<?> cycleOp) {
                dispenser = new EmitterCycleOpDispenserWrapper(adapter, pop, dispenser);
            } else {
                throw new OpConfigError("Unable to make op named '" + pop.getName() + "' emit a value, " +
                    "since only RunnableOp and CycleOp<Result> types are supported");
            }
            dispenser = new EmitterRunnableOpDispenserWrapper(
                (DriverAdapter<Op, Space>) adapter,
                pop,
                (OpDispenser<? extends Op>) dispenser
            );
            logger.warn(
                "initialized {} for to emit the result type to stdout. ",
                pop.getName()
            );

        }
        return dispenser;
    }
}
