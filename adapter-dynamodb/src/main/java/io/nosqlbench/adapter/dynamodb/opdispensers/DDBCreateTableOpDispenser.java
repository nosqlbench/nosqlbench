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
import com.amazonaws.services.dynamodbv2.model.*;
import io.nosqlbench.adapter.dynamodb.DynamoDBSpace;
import io.nosqlbench.adapter.dynamodb.optypes.DDBCreateTableOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * <pre>{@code
 * Request Syntax
 * {
 *    "AttributeDefinitions": [
 *       {
 *          "AttributeName": "string",
 *          "AttributeType": "string"
 *       }
 *    ],
 *    "BillingMode": "string",
 *    "GlobalSecondaryIndexes": [
 *       {
 *          "IndexName": "string",
 *          "KeySchema": [
 *             {
 *                "AttributeName": "string",
 *                "KeyType": "string"
 *             }
 *          ],
 *          "Projection": {
 *             "NonKeyAttributes": [ "string" ],
 *             "ProjectionType": "string"
 *          },
 *          "ProvisionedThroughput": {
 *             "ReadCapacityUnits": number,
 *             "WriteCapacityUnits": number
 *          }
 *       }
 *    ],
 *    "KeySchema": [
 *       {
 *          "AttributeName": "string",
 *          "KeyType": "string"
 *       }
 *    ],
 *    "LocalSecondaryIndexes": [
 *       {
 *          "IndexName": "string",
 *          "KeySchema": [
 *             {
 *                "AttributeName": "string",
 *                "KeyType": "string"
 *             }
 *          ],
 *          "Projection": {
 *             "NonKeyAttributes": [ "string" ],
 *             "ProjectionType": "string"
 *          }
 *       }
 *    ],
 *    "ProvisionedThroughput": {
 *       "ReadCapacityUnits": number,
 *       "WriteCapacityUnits": number
 *    },
 *    "SSESpecification": {
 *       "Enabled": boolean,
 *       "KMSMasterKeyId": "string",
 *       "SSEType": "string"
 *    },
 *    "StreamSpecification": {
 *       "StreamEnabled": boolean,
 *       "StreamViewType": "string"
 *    },
 *    "TableClass": "string",
 *    "TableName": "string",
 *    "Tags": [
 *       {
 *          "Key": "string",
 *          "Value": "string"
 *       }
 *    ]
 * }
 * }</pre>
 */
public class DDBCreateTableOpDispenser extends BaseOpDispenser<DynamoDBOp, DynamoDBSpace> {

    private final DynamoDB ddb;
    private final LongFunction<String> tableNameFunc;
    private final LongFunction<Collection<KeySchemaElement>> keySchemaFunc;
    private final LongFunction<Collection<AttributeDefinition>> attributeDefsFunc;
    private final LongFunction<String> readCapacityFunc;
    private final LongFunction<String> writeCapacityFunc;
    private final LongFunction<String> billingModeFunc;

    public DDBCreateTableOpDispenser(DriverAdapter adapter, DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(adapter,cmd);
        this.ddb = ddb;
        this.tableNameFunc = l -> targetFunc.apply(l).toString();
        this.keySchemaFunc = resolveKeySchemaFunction(cmd);
        this.attributeDefsFunc = resolveAttributeDefinitionFunction(cmd);
        this.billingModeFunc = cmd.getAsFunctionOr("BillingMode", BillingMode.PROVISIONED.name());
        this.readCapacityFunc = cmd.getAsFunctionOr("ReadCapacityUnits", "10");
        this.writeCapacityFunc = cmd.getAsFunctionOr("WriteCapacityUnits", "10");
    }

    @Override
    public DDBCreateTableOp apply(long cycle) {
        CreateTableRequest rq = new CreateTableRequest();
        rq.setTableName(tableNameFunc.apply(cycle));
        rq.setKeySchema(keySchemaFunc.apply(cycle));
        rq.setAttributeDefinitions(attributeDefsFunc.apply(cycle));
        rq.setBillingMode(BillingMode.valueOf(billingModeFunc.apply(cycle)).name());
        if (rq.getBillingMode().equals(BillingMode.PROVISIONED.name())) {
            rq.setProvisionedThroughput(
                new ProvisionedThroughput(
                    Long.parseLong(readCapacityFunc.apply(cycle)),
                    Long.parseLong(writeCapacityFunc.apply(cycle)))
            );
        }
        return new DDBCreateTableOp(ddb, rq);
    }

    private LongFunction<Collection<AttributeDefinition>> resolveAttributeDefinitionFunction(ParsedOp cmd) {
        LongFunction<? extends Map> attrsmap = cmd.getAsRequiredFunction("Attributes", Map.class);
        return (long l) -> {
            List<AttributeDefinition> defs = new ArrayList<>();
            attrsmap.apply(l).forEach((k, v) -> {
                defs.add(new AttributeDefinition(k.toString(), ScalarAttributeType.valueOf(v.toString())));
            });
            return defs;
        };
    }

    private LongFunction<Collection<KeySchemaElement>> resolveKeySchemaFunction(ParsedOp cmd) {
        LongFunction<? extends Map> keysmap = cmd.getAsRequiredFunction("Keys", Map.class);

        return (long l) -> {
            List<KeySchemaElement> elems = new ArrayList<>();
            keysmap.apply(l).forEach((k, v) -> {
                elems.add(new KeySchemaElement(k.toString(), KeyType.valueOf(v.toString())));
            });
            return elems;
        };
    }


}
