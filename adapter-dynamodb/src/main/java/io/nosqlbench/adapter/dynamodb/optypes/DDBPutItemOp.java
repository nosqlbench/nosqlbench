package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;

public class DDBPutItemOp extends DynamoDBOp {
    private final String tablename;
    private final Item item;

    public DDBPutItemOp(DynamoDB ddb, String tablename, Item item) {
        super(ddb);
        this.tablename = tablename;
        this.item = item;
    }

    @Override
    public PutItemOutcome apply(long value) {
        PutItemOutcome outcome = ddb.getTable(tablename).putItem(item);
        return outcome;
    }
}
