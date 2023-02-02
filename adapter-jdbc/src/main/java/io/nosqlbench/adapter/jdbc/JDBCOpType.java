package io.nosqlbench.adapter.jdbc;

/**
 * Op templates which are supported by the NoSQLBench CockroachDB driver are
 * enumerated below. These command names should mirror those in the official
 * CockroachDB API exactly. See the official API for more details.
 * @see <a href="https://www.cockroachlabs.com/docs/v22.2/sql-statements.html#data-definition-statements">CockroachDB API Reference</a>
 */
public enum JDBCOpType {
    //See https://jdbc.postgresql.org/documentation/query/
    select, // used for SELECT operation matches executeQuery
    update, // used for performing updates such as INSERT/UPDATE/DELETE matches executeUpdate
    ddl, // used for creating/modifying database objects matches execute
    //JdbcQuery, // generic placeholder TODO - implement this differently
    create, // used for CREATE operation matches execute
    drop, // used for DROP operation matches execute
}
