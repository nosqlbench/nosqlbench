package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Database;

public class DataApiDropCollectionOp extends DataApiBaseOp {
    private final String collectionName;
    public DataApiDropCollectionOp(Database db, String dbName) {
        super(db);
        this.collectionName = dbName;
    }

    @Override
    public Object apply(long value) {
        Boolean exists = db.collectionExists(collectionName);
        if (exists) {
            db.dropCollection(collectionName);
        }
        return exists;
    }
}
