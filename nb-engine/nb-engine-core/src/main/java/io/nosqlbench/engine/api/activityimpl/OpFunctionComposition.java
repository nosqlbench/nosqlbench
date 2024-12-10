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
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.*;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.nb.api.errors.ResultVerificationError;

/// This is where operations are customized to support special
/// testing or analysis capabilities. The behaviors here are not adapter-specific,
/// but can be specialized by adapter-provided logic in some cases.
///
/// In every case, the original dispenser and op instances are retained as in normal cycle
/// execution, although they may be used differently. For each new behavior, both the
/// original dispenser and the original op produced by it are wrapped by custom logic which can
/// functionally override or extend them.
///
/// The current behaviors which are supported are assembled in a fixed topology when used. In
/// other words, users can't simply layer customizations in any order or arrangement they choose.
///
/// To illustrate, an example of all of these together will be presented below:
///
/// ```
///     dryrun ( verify ( print ( capture ( op ))))
///```
///
/// This will change to be more in a future refinement, when all op synthesis modifiers will
/// be subsumed into [BaseOpDispenser]
///
/// Working from the outside-in, each is explained here:
/// * [DryrunOp] (dryrun) - Do not exeucute the operation, but prepare as if you were going to (
/// including any modifiers) and continue
/// * [AssertingOp] (verify) - apply any assertions specified by the user, and throw an
///  [ResultVerificationError] if the assertions are false, or
/// continue if assertions are true
/// * [ResultPrintingOp] (print) - Print the result of executing the op and continue
/// * [CapturingOp] (capture) - Capture the specified set of named fields from the result of the
/// operation, and continue
public class OpFunctionComposition {

    public final static Logger logger = LogManager.getLogger(OpFunctionComposition.class);

    public static <OP extends CycleOp<?>, SPACE extends Space> OpDispenser<? extends OP> wrapOptionally(
        DriverAdapter<? extends OP, ? extends SPACE> adapter, OpDispenser<? extends OP> dispenser,
        ParsedOp pop, Dryrun dryrun
    ) {

        dispenser = OpCapture.wrapOptionally(adapter, dispenser, pop);
        dispenser = OpAssertions.wrapOptionally(adapter, dispenser, pop);
        dispenser = OpDryrun.wrapOptionally(adapter, dispenser, pop, dryrun);

        return dispenser;
    }
}
