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

package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpResultSize;

public class DDBQueryOp extends DynamoDBOp implements OpResultSize {

    private final Table table;
    private final QuerySpec querySpec;
    private long resultSize = -1;

    public DDBQueryOp(DynamoDB ddb, Table table, QuerySpec querySpec) {
        super(ddb);
        this.table = table;
        this.querySpec = querySpec;
    }

    @Override
    public ItemCollection<QueryOutcome> apply(long value) {
        ItemCollection<QueryOutcome> result = table.query(querySpec);
        this.resultSize = result.getAccumulatedItemCount();
        return result;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
