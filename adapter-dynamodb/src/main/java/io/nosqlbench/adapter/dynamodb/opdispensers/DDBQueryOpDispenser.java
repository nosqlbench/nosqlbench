package io.nosqlbench.adapter.dynamodb.opdispensers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import io.nosqlbench.adapter.dynamodb.optypes.DDBQueryOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.Map;
import java.util.Optional;
import java.util.function.LongFunction;

/**
 * <pre>{@code
 * {
 *    "AttributesToGet": [ "string" ],
 *    "ConditionalOperator": "string",
 *    "ConsistentRead": boolean,
 *    "ExclusiveStartKey": {
 *       "string" : {
 *          "B": blob,
 *          "BOOL": boolean,
 *          "BS": [ blob ],
 *          "L": [
 *             "AttributeValue"
 *          ],
 *          "M": {
 *             "string" : "AttributeValue"
 *          },
 *          "N": "string",
 *          "NS": [ "string" ],
 *          "NULL": boolean,
 *          "S": "string",
 *          "SS": [ "string" ]
 *       }
 *    },
 *    "ExpressionAttributeNames": {
 *       "string" : "string"
 *    },
 *    "ExpressionAttributeValues": {
 *       "string" : {
 *          "B": blob,
 *          "BOOL": boolean,
 *          "BS": [ blob ],
 *          "L": [
 *             "AttributeValue"
 *          ],
 *          "M": {
 *             "string" : "AttributeValue"
 *          },
 *          "N": "string",
 *          "NS": [ "string" ],
 *          "NULL": boolean,
 *          "S": "string",
 *          "SS": [ "string" ]
 *       }
 *    },
 *    "FilterExpression": "string",
 *    "IndexName": "string",
 *    "KeyConditionExpression": "string",
 *    "KeyConditions": {
 *       "string" : {
 *          "AttributeValueList": [
 *             {
 *                "B": blob,
 *                "BOOL": boolean,
 *                "BS": [ blob ],
 *                "L": [
 *                   "AttributeValue"
 *                ],
 *                "M": {
 *                   "string" : "AttributeValue"
 *                },
 *                "N": "string",
 *                "NS": [ "string" ],
 *                "NULL": boolean,
 *                "S": "string",
 *                "SS": [ "string" ]
 *             }
 *          ],
 *          "ComparisonOperator": "string"
 *       }
 *    },
 *    "Limit": number,
 *    "ProjectionExpression": "string",
 *    "QueryFilter": {
 *       "string" : {
 *          "AttributeValueList": [
 *             {
 *                "B": blob,
 *                "BOOL": boolean,
 *                "BS": [ blob ],
 *                "L": [
 *                   "AttributeValue"
 *                ],
 *                "M": {
 *                   "string" : "AttributeValue"
 *                },
 *                "N": "string",
 *                "NS": [ "string" ],
 *                "NULL": boolean,
 *                "S": "string",
 *                "SS": [ "string" ]
 *             }
 *          ],
 *          "ComparisonOperator": "string"
 *       }
 *    },
 *    "ReturnConsumedCapacity": "string",
 *    "ScanIndexForward": boolean,
 *    "Select": "string",
 *    "TableName": "string"
 * }
 * }</pre>
 */
public class DDBQueryOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final DynamoDB ddb;
    private final LongFunction<Table> tableFunc;
    private final LongFunction<QuerySpec> querySpecFunc;

    public DDBQueryOpDispenser(DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunc) {
        super(cmd);
        this.ddb = ddb;
        LongFunction<String> tableNameFunc = l -> targetFunc.apply(l).toString();
        this.tableFunc = l -> ddb.getTable(tableNameFunc.apply(l));

        this.querySpecFunc = resolveQuerySpecFunc(cmd);
    }

    @Override
    public DDBQueryOp apply(long cycle) {
        Table table = tableFunc.apply(cycle);
        QuerySpec queryspec = querySpecFunc.apply(cycle);
        return new DDBQueryOp(ddb,table,queryspec);
    }

    private LongFunction<QuerySpec> resolveQuerySpecFunc(ParsedOp cmd) {

        LongFunction<QuerySpec> func = l -> new QuerySpec();

        Optional<LongFunction<String>> projFunc = cmd.getAsOptionalFunction("projection", String.class);
        if (projFunc.isPresent()) {
            LongFunction<QuerySpec> finalFunc = func;
            LongFunction<String> af = projFunc.get();
            func = l -> finalFunc.apply(l).withAttributesToGet(af.apply(l));
        }

        Optional<LongFunction<Boolean>> consistentRead = cmd.getAsOptionalFunction("ConsistentRead", boolean.class);
        if (consistentRead.isPresent()) {
            LongFunction<QuerySpec> finalFunc = func;
            LongFunction<Boolean> consistentReadFunc = consistentRead.get();
            func = l -> finalFunc.apply(l).withConsistentRead(consistentReadFunc.apply(l));
        }

        Optional<LongFunction<Map>> exclStrtKeyFunc = cmd.getAsOptionalFunction("ExclusiveStartKey",Map.class);
        if (exclStrtKeyFunc.isPresent()) {
            LongFunction<QuerySpec> finalFunc = func;
            LongFunction<Map> skf = exclStrtKeyFunc.get();
            LongFunction<PrimaryKey> pkf = l -> {
                PrimaryKey pk = new PrimaryKey();
                skf.apply(l).forEach((k,v) -> pk.addComponent(k.toString(),v.toString()));
                return pk;
            };
            func = l -> finalFunc.apply(l).withExclusiveStartKey(pkf.apply(l));
        }

        Optional<LongFunction<Integer>> limitFunc = cmd.getAsOptionalFunction("Limit",Integer.class);
        if (limitFunc.isPresent()) {
            LongFunction<Integer> limitf = limitFunc.get();
            LongFunction<QuerySpec> finalFunc = func;
            func = l -> finalFunc.apply(l).withMaxResultSize(limitf.apply(l));
        }

        return func;
    }

}
