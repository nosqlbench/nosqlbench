package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

/**
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_GetItem.html#API_GetItem_RequestSyntax">GetItem API</a>
 * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Expressions.Attributes.html">Expressions.Attributes</a>
 */
public class DDBGetItemOp extends DynamoDBOp {
    private final Table table;
    private GetItemSpec getItemSpec;
    private long resultSize=-1;

    public DDBGetItemOp(DynamoDB ddb, Table table, GetItemSpec getItemSpec) {
        super(ddb);
        this.table = table;
        this.getItemSpec = getItemSpec;
    }

    @Override
    public Item apply(long value) {
        Item result = table.getItem(getItemSpec);
        resultSize=result.numberOfAttributes();
        return result;
    }

    @Override
    public long getResultSize() {
        return resultSize;
    }
}
