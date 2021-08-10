package io.nosqlbench.adapter.cqld4.processors;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;

import java.util.ArrayList;

/**
 * An accumulator for rows, sized to a page of results.
 */
public class RSIterableCapture implements ResultSetProcessor {

    private long cycle;
    private ArrayList<Row> rows = new ArrayList<>();

    @Override
    public void start(long cycle, ResultSet container) {
        this.cycle = cycle;
        rows = new ArrayList<Row>(container.getAvailableWithoutFetching());
    }

    @Override
    public void buffer(Row element) {
        rows.add(element);

    }

    @Override
    public void flush() {

    }
}
