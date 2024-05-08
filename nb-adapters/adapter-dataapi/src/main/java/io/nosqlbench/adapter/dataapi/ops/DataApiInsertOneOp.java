package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;

public class DataApiInsertOneOp extends DataApiBaseOp {
    private final Document doc;
    private final String collectionName;

    public DataApiInsertOneOp(Database db, String collectionName, Document doc) {
        super(db);
        this.collectionName = collectionName;
        this.doc = doc;
    }

    @Override
    public Object apply(long value) {
        return db.getCollection(collectionName).insertOne(doc);
    }
}
