package io.nosqlbench.adapter.dataapi.ops;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.Database;
import com.datastax.astra.client.model.Filter;

public class DataApiDeleteManyOp extends DataApiBaseOp {
    private final Collection collection;
    private final Filter filter;

    public DataApiDeleteManyOp(Database db, Collection collection, Filter filter) {
        super(db);
        this.collection = collection;
        this.filter = filter;
    }

    @Override
    public Object apply(long value) {
        return collection.deleteMany(filter);
    }
}
