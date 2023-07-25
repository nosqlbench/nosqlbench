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
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

/**
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.ReadWriteCapacityMode.html?icmpid=docs_dynamodb_help_panel_hp_capacity#HowItWorks.ProvisionedThroughput.Manual
 */
public abstract class DynamoDBOp implements CycleOp<Object> {

    protected DynamoDB ddb;

    public DynamoDBOp(DynamoDB ddb) {
        this.ddb = ddb;
    }

}
