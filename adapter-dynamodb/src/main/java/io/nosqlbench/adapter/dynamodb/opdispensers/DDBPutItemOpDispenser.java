package io.nosqlbench.adapter.dynamodb.opdispensers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import io.nosqlbench.adapter.dynamodb.optypes.DDBPutItemOp;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.Map;
import java.util.function.LongFunction;

public class DDBPutItemOpDispenser implements OpDispenser<DynamoDBOp> {

    private final DynamoDB ddb;
    private final LongFunction<String> tableNameFunc;
    private final LongFunction<? extends Item> itemfunc;

    public DDBPutItemOpDispenser(DynamoDB ddb, ParsedOp cmd, LongFunction<?> targetFunc) {
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
