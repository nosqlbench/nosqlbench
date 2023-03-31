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

import io.nosqlbench.adapter.mongodb.dispensers.MongoCommandOpDispenser;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.LongFunction;

public class MongoOpMapper implements OpMapper<Op> {
    private static final Logger logger = LogManager.getLogger(MongoOpMapper.class);

    private final MongodbDriverAdapter adapter;

    public MongoOpMapper(MongodbDriverAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {

        LongFunction<String> ctxNamer = op.getAsFunctionOr("space", "default");
        String connectionValue = op.getStaticConfigOr("connection", "unknown");

        if (connectionValue == null) {
            throw new BasicError("Must provide a connection value for use be MongoDB adapter.");
        }
        adapter.setConnection(connectionValue);

        LongFunction<MongoSpace> spaceF = l -> adapter.getSpaceCache().get(ctxNamer.apply(l));
        Optional<LongFunction<String>> oDatabaseF = op.getAsOptionalFunction("database");
        if (oDatabaseF.isEmpty()) {
            logger.warn("op field 'database' was not defined");
        }

        Optional<TypeAndTarget<MongoDBOpTypes, String>> target = op.getOptionalTypeAndTargetEnum(MongoDBOpTypes.class, String.class);

        // For any of the named operations which are called out directly AND supported via the fluent API,
        // use specialized dispensers
        if (target.isPresent()) {
            TypeAndTarget<MongoDBOpTypes, String> targetData = target.get();
            return switch (targetData.enumId) {
                case command -> new MongoCommandOpDispenser(adapter, spaceF, op);
            };
        }
        // For everything else use the command API
        else {
            return new MongoCommandOpDispenser(adapter, spaceF, op);
        }


    }
}
