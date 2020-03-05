package io.nosqlbench.activitytype.cql.statements.rsoperators;

import com.datastax.driver.core.Row;

import java.util.LinkedList;

public class PerThreadCQLData {
    public final static ThreadLocal<LinkedList<Row>> rows = ThreadLocal.withInitial(LinkedList::new);
}
