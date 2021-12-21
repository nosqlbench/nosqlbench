package io.nosqlbench.adapter.dynamodb.opdispensers;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.adapter.dynamodb.optypes.RawDynamodOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class RawDynamoDBOpDispenser extends BaseOpDispenser<DynamoDBOp> {

    private final LongFunction<? extends String> jsonFunction;
    private final DynamoDB ddb;

    public RawDynamoDBOpDispenser(DynamoDB ddb, ParsedOp pop) {
        super(pop);
        this.ddb = ddb;

        String bodytype = pop.getValueType("body").getSimpleName();
        switch (bodytype) {
            case "String":
                jsonFunction=pop.getAsRequiredFunction("body");
                break;
            default:
                throw new RuntimeException("Unable to create body mapping function from type '" + bodytype + "'");
        }
    }

    @Override
    public DynamoDBOp apply(long value) {
        String body = jsonFunction.apply(value);
        return new RawDynamodOp(ddb,body);
    }
}
