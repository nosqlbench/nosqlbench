/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.adapter.mongodb.ops.MongoDirectCommandOp;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.LongFunction;

public class MongoOpMapper<MC extends MongoDirectCommandOp> implements OpMapper<MongoDirectCommandOp,MongoSpace> {
    private static final Logger logger = LogManager.getLogger(MongoOpMapper.class);

    private final MongodbDriverAdapter adapter;
    private final NBConfiguration configuration;

    public MongoOpMapper(MongodbDriverAdapter adapter, NBConfiguration cfg) {
        this.configuration = cfg;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<MongoDirectCommandOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<MongoSpace> spaceInitF) {

        LongFunction<String> ctxNamer = op.getAsFunctionOr("space", "default");

        String connectionURL = op.getStaticConfigOr("connection", "unknown");
        if (connectionURL == null) {
            throw new BasicError("Must provide a connection value for use by the MongoDB adapter.");
        }

        Optional<LongFunction<String>> oDatabaseF = op.getAsOptionalFunction("database");
        if (oDatabaseF.isEmpty()) {
            logger.warn("op field 'database' was not defined");
        }

        Optional<TypeAndTarget<MongoDBOpTypes, String>> target = op.getOptionalTypeAndTargetEnum(MongoDBOpTypes.class,
                String.class);

        // For any of the named operations which are called out directly AND supported via the fluent API,
        // use specialized dispensers
        if (target.isPresent()) {
            TypeAndTarget<MongoDBOpTypes, String> targetData = target.get();
            return switch (targetData.enumId) {
                case command -> new MongoCommandOpDispenser(adapter, spaceInitF, op);
            };
        }
        // For everything else use the command API
        else {
            return new MongoCommandOpDispenser(adapter, spaceInitF, op);
        }


    }

}
