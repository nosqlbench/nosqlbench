package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;

public class DDBCreateTableOp extends DynamoDBOp {

    private final CreateTableRequest rq;

    public DDBCreateTableOp(DynamoDB ddb, CreateTableRequest rq) {
        super(ddb);
        this.rq = rq;
    }

    @Override
    public Table apply(long value) {
        return ddb.createTable(rq);
    }
}
