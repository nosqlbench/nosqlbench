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

package io.nosqlbench.adapter.dynamodb.opdispensers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import io.nosqlbench.adapter.dynamodb.DynamoDBSpace;
import io.nosqlbench.adapter.dynamodb.optypes.DDBDeleteTableOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

/**
 * <pre>{@code
 * Request Syntax
 * {
 *    "TableName": "string"
 * }
 * }</pre>
 */
public class DDBDeleteTableOpDispenser extends BaseOpDispenser<DynamoDBOp, DynamoDBSpace> {

    private final DynamoDB ddb;
    private final LongFunction<String> tableNameFunc;

    public DDBDeleteTableOpDispenser(DriverAdapter adapter, DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(adapter, cmd);
        this.ddb = ddb;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
    }

    @Override
    public DDBDeleteTableOp apply(long cycle) {
        DeleteTableRequest rq = new DeleteTableRequest();
        rq.setTableName(tableNameFunc.apply(cycle));
        return new DDBDeleteTableOp(ddb, rq);
    }

}
