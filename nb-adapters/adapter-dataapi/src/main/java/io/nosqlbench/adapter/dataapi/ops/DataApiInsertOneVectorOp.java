package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;

public class DataApiInsertOneVectorOp extends DataApiBaseOp {
    private final Document doc;
    private final String collectionName;
    private float[] vector;

    public DataApiInsertOneVectorOp(Database db, String collectionName, Document doc, float[] vector) {
        super(db);
        this.collectionName = collectionName;
        this.doc = doc;
        this.vector = vector;
    }

    @Override
    public Object apply(long value) {
        return db.getCollection(collectionName).insertOne(doc, vector);
    }
}
