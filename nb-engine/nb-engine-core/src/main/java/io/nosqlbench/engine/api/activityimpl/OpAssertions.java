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
import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;
import io.nosqlbench.adapters.api.activityimpl.uniform.ValidatorSource;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.AssertingOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/// This is a functional wrapper layer which will upgrade a basic [CycleOp] to
/// one that has a validator provided by it's [DriverAdapter], so long as the
/// adapter implements [ValidatorSource] and can provide one for the given name.
public class OpAssertions {

    public final static Logger logger = LogManager.getLogger(OpAssertions.class);

    public static <OP extends CycleOp<?>, SPACE extends Space> OpDispenser<? extends OP> wrapOptionally(
        DriverAdapter<? extends OP, ? extends SPACE> adapter, OpDispenser<? extends OP> dispenser,
        ParsedOp pop
    ) {

        Optional<String> validatorName = pop.takeOptionalStaticValue("validator", String.class);
        if (validatorName.isEmpty()) return dispenser;

        if (adapter instanceof ValidatorSource vs) {
            Optional<Validator> validator = vs.getValidator(validatorName.get(),pop);
            if (validator.isEmpty()) {
                throw new OpConfigError(
                    "a validator '" + validatorName.get() + "' was requested, but adapter '" + adapter.getAdapterName() + "' did not find it.");
            }
            return new AssertingOpDispenser(adapter, pop, dispenser, validator.get());
        } else {
            throw new OpConfigError(
                "a validator '" + validatorName.get() + "' was specified, " + "but the adapter '" + adapter.getAdapterName() + "' does " + "not implement " + adapter.getClass().getSimpleName());
        }
    }
}
