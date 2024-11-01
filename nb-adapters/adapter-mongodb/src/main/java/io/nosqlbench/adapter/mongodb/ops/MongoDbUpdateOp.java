package io.nosqlbench.adapter.mongodb.ops;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.bson.Document;

public class MongoDbUpdateOp implements CycleOp<Document> {

    @Override
    public Document apply(long value) {
        return null;
    }
}
