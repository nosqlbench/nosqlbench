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
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import io.nosqlbench.adapter.dynamodb.DynamoDBSpace;
import io.nosqlbench.adapter.dynamodb.optypes.DDBGetItemOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

public class DDBGetItemOpDispenser extends BaseOpDispenser<DynamoDBOp, DynamoDBSpace> {
    private final DynamoDB ddb;
    private final LongFunction<Table> targetTableFunction;
    private final LongFunction<GetItemSpec> getItemSpecFunc;

    public DDBGetItemOpDispenser(DriverAdapter adapter, DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunction) {
        super(adapter,cmd);
        this.ddb = ddb;
        this.targetTableFunction = l -> ddb.getTable(targetFunction.apply(l).toString());
        this.getItemSpecFunc = resolveGetItemSpecFunction(cmd);
    }

    private LongFunction<GetItemSpec> resolveGetItemSpecFunction(ParsedOp cmd) {

        PrimaryKey primaryKey = null;
        LongFunction<PrimaryKey> pkfunc = null;
        String projection = null;
        LongFunction<String> projfunc = null;

        LongFunction<? extends Map> keysmap_func = cmd.getAsRequiredFunction("key",Map.class);
        LongFunction<PrimaryKey> pk_func = l -> {
            PrimaryKey pk = new PrimaryKey();
            keysmap_func.apply(l).forEach((k,v) -> {
                pk.addComponent(k.toString(),v);
            });
            return pk;
        };
        LongFunction<GetItemSpec> gis = l -> new GetItemSpec().withPrimaryKey(pk_func.apply(l));

        Optional<LongFunction<String>> projection_func = cmd.getAsOptionalFunction("projection",String.class);
        if (projection_func.isPresent()) {
            LongFunction<GetItemSpec> finalGis = gis;
            gis = l -> {
                LongFunction<String> pj = projection_func.get();
                return finalGis.apply(l).withProjectionExpression(pj.apply(1));
            };
        }

        Optional<LongFunction<Boolean>> consistentRead = cmd.getAsOptionalFunction("ConsistentRead", boolean.class);
        if (consistentRead.isPresent()) {
            LongFunction<GetItemSpec> finalGis = gis;
            gis = l -> {
                LongFunction<Boolean> consistentReadFunc = consistentRead.get();
                return finalGis.apply(l).withConsistentRead(consistentReadFunc.apply(l));
            };
        }

        return gis;

    }

    @Override
    public DDBGetItemOp apply(long value) {
        Table table = targetTableFunction.apply(value);
        GetItemSpec getitemSpec = getItemSpecFunc.apply(value);
        return new DDBGetItemOp(ddb, table, getitemSpec);
    }
}
