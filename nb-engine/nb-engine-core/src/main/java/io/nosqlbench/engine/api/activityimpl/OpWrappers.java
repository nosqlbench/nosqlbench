package io.nosqlbench.engine.api.activityimpl;

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


import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.DryCycleOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.EmitterCycleOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.EmitterRunnableOpDispenserWrapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpWrappers {

    public final static Logger logger = LogManager.getLogger(OpWrappers.class);

    public static <OP extends CycleOp<?>, SPACE extends Space> OpDispenser<OP> wrapOptionally(
        DriverAdapter<OP, SPACE> adapter,
        OpDispenser<OP> dispenser,
        ParsedOp pop,
        String dryrunSpec
    ) {
        Dryrun dryrun = Dryrun.valueOf(dryrunSpec);
        return switch (dryrun) {
            case none -> dispenser;
            case op -> new DryCycleOpDispenserWrapper(adapter, pop, dispenser);
            case emit -> new EmitterCycleOpDispenserWrapper(adapter, pop, dispenser);
        };
    }
}
