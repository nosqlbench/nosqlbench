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

package io.nosqlbench.adapter.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.nosqlbench.adapter.dynamodb.opdispensers.*;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;

import java.util.function.LongFunction;

public class DynamoDBOpMapper implements OpMapper<DynamoDBOp,DynamoDBSpace> {

    private final NBConfiguration cfg;
    private final DriverAdapter adapter;

    public DynamoDBOpMapper(DynamoDBDriverAdapter adapter, NBConfiguration cfg) {
        this.cfg = cfg;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<DynamoDBOp> apply(NBComponent adapterC, ParsedOp op, LongFunction<DynamoDBSpace> spaceInitF) {
        int space = op.getStaticConfigOr("space", 0);
        LongFunction<DynamoDBSpace> spaceFunc = adapter.getSpaceFunc(op);
        DynamoDB ddb = spaceFunc.apply(space).getDynamoDB();

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (op.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
//            return new RawDynamoDBOpDispenser(cmd);
        } else {
            TypeAndTarget<DynamoDBCmdType,String> cmdType = op.getTypeAndTarget(DynamoDBCmdType.class,String.class);
            return switch (cmdType.enumId) {
                case CreateTable -> new DDBCreateTableOpDispenser(adapter, ddb, op, cmdType.targetFunction);
                case DeleteTable -> new DDBDeleteTableOpDispenser(adapter, ddb, op, cmdType.targetFunction);
                case PutItem -> new DDBPutItemOpDispenser(adapter, ddb, op, cmdType.targetFunction);
                case GetItem -> new DDBGetItemOpDispenser(adapter, ddb, op, cmdType.targetFunction);
                case Query -> new DDBQueryOpDispenser(adapter,ddb, op, cmdType.targetFunction);
            };
        }

    }

}
