package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Document;
import com.datastax.astra.client.model.Filter;
import com.datastax.astra.client.model.FindOneOptions;

public class DataApiFindOneOp extends DataApiBaseOp {
    private final Collection<Document> collection;
    private final Filter filter;
    private final FindOneOptions options;

    public DataApiFindOneOp(Database db, Collection<Document> collection, Filter filter, FindOneOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.findOne(filter, options);
    }
}
