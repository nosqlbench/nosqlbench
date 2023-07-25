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
import com.amazonaws.services.dynamodbv2.document.Item;
import io.nosqlbench.adapter.dynamodb.DynamoDBSpace;
import io.nosqlbench.adapter.dynamodb.optypes.DDBPutItemOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.api.errors.OpConfigError;

import java.util.Map;
import java.util.function.LongFunction;

public class DDBPutItemOpDispenser extends BaseOpDispenser<DynamoDBOp, DynamoDBSpace> {

    private final DynamoDB ddb;
    private final LongFunction<String> tableNameFunc;
    private final LongFunction<? extends Item> itemfunc;

    public DDBPutItemOpDispenser(DriverAdapter adapter, DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(adapter, cmd);
        this.ddb = ddb;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
        if (cmd.isDefined("item")) {
            LongFunction<? extends Map> f1 = cmd.getAsRequiredFunction("item", Map.class);
            this.itemfunc = l -> Item.fromMap(f1.apply(l));
        } else if (cmd.isDefined("json")) {
            LongFunction<? extends String> f1 = cmd.getAsRequiredFunction("json", String.class);
            this.itemfunc = l -> Item.fromJSON(f1.apply(l));
        } else {
            throw new OpConfigError("PutItem op templates require either an 'item' map field or a 'json' text field");
        }
    }

    @Override
    public DynamoDBOp apply(long value) {
        String tablename = tableNameFunc.apply(value);
        Item item = itemfunc.apply(value);
        return new DDBPutItemOp(ddb,tablename,item);
    }
}
