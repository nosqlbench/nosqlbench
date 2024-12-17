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
import io.nosqlbench.adapters.api.activityimpl.OpLookup;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// This is a functional wrapper layer which will upgrade a basic [CycleOp] to
/// one that has a validator provided by it's [DriverAdapter], so long as the
/// adapter implements [ValidatorSource] and can provide one for the given name.
public class OpAssertions {

    public final static Logger logger = LogManager.getLogger(OpAssertions.class);

    public static <OP extends CycleOp<?>, SPACE extends Space> OpDispenser<? extends OP> wrapOptionally(
        DriverAdapter<? extends OP, ? extends SPACE> adapter,
        OpDispenser<? extends OP> dispenser,
        ParsedOp pop,
        OpLookup lookup
    ) {

//        Optional<String> validatorName = pop.takeOptionalStaticValue("validators", String.class);
//        if (validatorName.isEmpty()) return dispenser;

        List<ValidatorSource> sources = new ArrayList<>();
        if (adapter instanceof ValidatorSource s) {
            sources.add(s);
        }
        if (dispenser instanceof ValidatorSource s) {
            sources.add(s);
        }

        for (ValidatorSource source : sources) {
            List<Validator> validator = source.getValidator(adapter, pop, lookup);
            for (Validator v : validator) {
                dispenser = new AssertingOpDispenser(adapter, pop, dispenser, v);
                logger.trace("added post-run validator for op '" + pop.getName() + "'");
            }
        }

        return dispenser;
    }
}
