package io.nosqlbench.adapter.cqld4.processors;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.adapter.cqld4.ResultSetProcessor;
import io.nosqlbench.virtdata.core.templates.CapturePoint;

import java.util.List;

public class CqlFieldCaptureProcessor implements ResultSetProcessor {

    private final List<CapturePoint> captures;

    public CqlFieldCaptureProcessor(List<CapturePoint> captures) {
        this.captures = captures;
    }

    @Override
    public void start(long cycle, ResultSet container) {

    }

    @Override
    public void buffer(Row element) {

    }

    @Override
    public void flush() {

    }
}
