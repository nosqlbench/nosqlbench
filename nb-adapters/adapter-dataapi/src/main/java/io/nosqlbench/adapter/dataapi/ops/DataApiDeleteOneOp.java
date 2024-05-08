package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.DeleteOneOptions;
import com.datastax.astra.client.model.Filter;

public class DataApiDeleteOneOp extends DataApiBaseOp {
    private final Collection collection;
    private final Filter filter;
    private final DeleteOneOptions options;

    public DataApiDeleteOneOp(Database db, Collection collection, Filter filter, DeleteOneOptions options) {
        super(db);
        this.collection = collection;
        this.filter = filter;
        this.options = options;
    }

    @Override
    public Object apply(long value) {
        return collection.deleteOne(filter, options);
    }
}
