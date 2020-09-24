package io.nosqlbench.generators.cql.lang;

public class CqlWorkloadBuffer {
    public void newTable(String keyspace, String table) {
    }

    /**
     * type is the parsed tokens of the type definition,
     * with each type token and each bracket taking a position.
     * For example, both <pre>{@code
     *  "timeuuid"
     * }</pre> and <pre>{@code
     *  "set","<","text",">"
     *  }</pre> are valid. This is just an opaque transfer type to
     * allow simple decoupling of the upstream parser and the workload
     * generator.
     *
     * @param colname The name of the column
     * @param type    A token stream representing the type of the column
     */
    public void newColumn(String colname, String... type) {
    }
}
