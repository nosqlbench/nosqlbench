package io.nosqlbench.adapter.cqld4;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;

import java.util.Map;

public class Cqld4PrintProcessor implements ResultSetProcessor {

    StringBuilder sb = new StringBuilder();

    public Cqld4PrintProcessor(Map<String, ?> cfg) {
    }

    @Override
    public void start(long cycle, ResultSet container) {
        sb.setLength(0);
        sb.append("c[").append(cycle).append("] ");
    }

    @Override
    public void buffer(Row element) {
        sb.append(element.getFormattedContents()).append("\n");
    }

    @Override
    public void flush() {
        System.out.print(sb.toString());
    }
}
