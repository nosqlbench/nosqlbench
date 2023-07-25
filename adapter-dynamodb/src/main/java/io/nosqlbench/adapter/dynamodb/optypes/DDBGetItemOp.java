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
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.OpResultSize;

/**
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_GetItem.html#API_GetItem_RequestSyntax">GetItem API</a>
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.Attributes.html">Expressions.Attributes</a>
 */
public class DDBGetItemOp extends DynamoDBOp implements OpResultSize {
    private final Table table;
    private final GetItemSpec getItemSpec;
    private long resultSize=0;

    public DDBGetItemOp(DynamoDB ddb, Table table, GetItemSpec getItemSpec) {
        super(ddb);
        this.table = table;
        this.getItemSpec = getItemSpec;
    }

    @Override
    public Item apply(long value) {
        Item result = table.getItem(getItemSpec);
        if (result!=null) {
            resultSize=result.numberOfAttributes();
        }
        return result;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
