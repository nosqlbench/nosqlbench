package io.nosqlbench.adapters.api.activityimpl;

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


import io.nosqlbench.adapters.api.activityimpl.uniform.Validator;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoreOpValidators {
    private static final Logger logger = LogManager.getLogger(CoreOpValidators.class);

    public static List<Validator> getValidator(NBComponent parent, ParsedOp pop, OpLookup lookup) {
        List<Validator> validators = new ArrayList();
        Optional<TypeAndTarget<CoreValidators, Object>> optionalValidator = pop.getOptionalTypeAndTargetEnum(
            CoreValidators.class, Object.class);

        if (optionalValidator.isPresent()) {
            TypeAndTarget<CoreValidators, Object> validator = optionalValidator.get();
            logger.debug("found validator '" + validator.enumId.name() + "' for op '" + pop.getName() + "'");
            switch (validator.enumId) {
                case verify_fields:
                    validators.add(new FieldVerifier(parent, pop, lookup));
            }
        }

        return validators;
    }
}
