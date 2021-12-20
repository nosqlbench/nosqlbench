package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class RawDynamodOp extends DynamoDBOp {
    public RawDynamodOp(DynamoDB ddb, String body) {
        super(ddb);
    }

    @Override
    public Table apply(long value) {
        throw new RuntimeException("raw ops are not supported in this API yet");
    }
}
