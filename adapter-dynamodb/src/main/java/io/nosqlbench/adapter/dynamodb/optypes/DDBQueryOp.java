package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;

public class DDBQueryOp extends DynamoDBOp {

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
