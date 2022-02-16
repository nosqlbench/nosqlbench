package io.nosqlbench.adapter.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.nosqlbench.adapter.dynamodb.opdispensers.DDBCreateTableOpDispenser;
import io.nosqlbench.adapter.dynamodb.opdispensers.DDBGetItemOpDispenser;
import io.nosqlbench.adapter.dynamodb.opdispensers.DDBPutItemOpDispenser;
import io.nosqlbench.adapter.dynamodb.opdispensers.DDBQueryOpDispenser;
import io.nosqlbench.adapter.dynamodb.optypes.DynamoDBOp;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.templating.TypeAndTarget;
import io.nosqlbench.engine.api.templating.ParsedOp;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.errors.OpConfigError;

public class DynamoDBOpMapper implements OpMapper<DynamoDBOp> {

    private final NBConfiguration cfg;
    private final DriverSpaceCache<? extends DynamoDBSpace> cache;

    public DynamoDBOpMapper(NBConfiguration cfg, DriverSpaceCache<? extends DynamoDBSpace> cache) {
        this.cfg = cfg;
        this.cache = cache;
    }

    @Override
    public OpDispenser<DynamoDBOp> apply(ParsedOp cmd) {
        String space = cmd.getStaticConfigOr("space", "default");
        DynamoDB ddb = cache.get(space).getDynamoDB();

        /*
         * If the user provides a body element, then they want to provide the JSON or
         * a data structure that can be converted into JSON, bypassing any further
         * specialized type-checking or op-type specific features
         */
        if (cmd.isDefined("body")) {
            throw new RuntimeException("This mode is reserved for later. Do not use the 'body' op field.");
//            return new RawDynamoDBOpDispenser(cmd);
        } else {
            TypeAndTarget<DynamoDBCmdType,String> cmdType = cmd.getTargetEnum(DynamoDBCmdType.class,String.class);
            switch (cmdType.enumId) {
                case CreateTable:
                    return new DDBCreateTableOpDispenser(ddb, cmd, cmdType.targetFunction);
                case PutItem:
                    return new DDBPutItemOpDispenser(ddb, cmd, cmdType.targetFunction);
                case GetItem:
                    return new DDBGetItemOpDispenser(ddb, cmd, cmdType.targetFunction);
                case Query:
                    return new DDBQueryOpDispenser(ddb, cmd, cmdType.targetFunction);

                default:
                    throw new OpConfigError("No implementation for " + cmdType);
            }
        }

    }

}
