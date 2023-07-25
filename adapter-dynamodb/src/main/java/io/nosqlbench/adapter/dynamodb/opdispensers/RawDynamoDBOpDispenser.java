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
import io.nosqlbench.adapter.dynamodb.DynamoDBSpace;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapter.dynamodb.optypes.RawDynamodOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class RawDynamoDBOpDispenser extends BaseOpDispenser<DynamoDBOp, DynamoDBSpace> {

    private final LongFunction<? extends String> jsonFunction;
    private final DynamoDB ddb;

    public RawDynamoDBOpDispenser(DriverAdapter adapter, DynamoDB ddb, ParsedOp pop) {
        super(adapter,pop);
        this.ddb = ddb;

        String bodytype = pop.getValueType("body").getSimpleName();
        if (bodytype.equals("String")) {
            jsonFunction = pop.getAsRequiredFunction("body");
        } else {
            throw new RuntimeException("Unable to create body mapping function from type '" + bodytype + "'");
        }
    }

    @Override
    public DynamoDBOp apply(long value) {
        String body = jsonFunction.apply(value);
        return new RawDynamodOp(ddb,body);
    }
}
