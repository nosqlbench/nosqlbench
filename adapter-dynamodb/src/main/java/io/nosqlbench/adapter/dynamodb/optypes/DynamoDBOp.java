package io.nosqlbench.adapter.dynamodb.optypes;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.CycleOp;

/**
 * https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.ReadWriteCapacityMode.html?icmpid=docs_dynamodb_help_panel_hp_capacity#HowItWorks.ProvisionedThroughput.Manual
 */
public abstract class DynamoDBOp implements CycleOp<Object> {

    protected DynamoDB ddb;

    public DynamoDBOp(DynamoDB ddb) {
        this.ddb = ddb;
    }

}
