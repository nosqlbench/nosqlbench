/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.mongodb.core;

import io.nosqlbench.adapter.mongodb.dispensers.MongoDbUpdateOpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.LongFunction;

public class MongodbOpMapper implements OpMapper<Op> {
    private final static Logger logger = LogManager.getLogger(MongodbOpMapper.class);

    private final MongodbDriverAdapter adapter;

    public MongodbOpMapper(MongodbDriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {
        LongFunction<String> ctxNamer = op.getAsFunctionOr("space", "default");
        LongFunction<MongoSpace> spaceF = l -> adapter.getSpaceCache().get(ctxNamer.apply(l));

        TypeAndTarget<MongoDBOpTypes, String> opTypeAndTarget =
            op.getOptionalTypeAndTargetEnum(MongoDBOpTypes.class, String.class)
                .orElseThrow(() -> new RuntimeException("unable to determine MongoDB op type from '" + op.toString()));
        Optional<LongFunction<String>> oDatabaseF = op.getAsOptionalFunction("database");
        if (oDatabaseF.isEmpty()) {
            logger.warn(() -> "");
        }


        return switch (opTypeAndTarget.enumId) {
            case command -> null;
            case update -> new MongoDbUpdateOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
//            case insert -> new MongoDbInsertOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
//            case delete -> new MongoDbDeleteOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
//            case find -> new mongoDbFindOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
//            case findAndModify -> new MongoDbFindAndModifyOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
//            case getMore -> new MongoDbGetMoreOpDispenser(adapter, op, opTypeAndTarget.targetFunction);
        };

    }
}
