package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.FindOneOptions;
import com.datastax.astra.client.model.FindOptions;

public class DataApiFindOp extends DataApiBaseOp {
    private final Collection<Document> collection;
    private final Filter filter;
    private final FindOptions options;

    public DataApiFindOp(Database db, Collection<Document> collection, Filter filter, FindOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.find(filter, options);
    }
}
