/*
 * Copyright (c) 2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.jdbc;

/**
 * Op templates which are supported by the NoSQLBench CockroachDB driver are
 * enumerated below. These command names should mirror those in the official
 * CockroachDB API exactly. See the official API for more details.
 * @see <a href="https://www.cockroachlabs.com/docs/v22.2/sql-statements.html#data-definition-statements">CockroachDB API Reference</a>
 */
public enum JDBCOpType {
    //See https://jdbc.postgresql.org/documentation/query/
    executeQuery, // used for SELECT operation matches executeQuery
    executeUpdate, // used for performing updates such as INSERT/UPDATE/DELETE matches executeUpdate
    execute, // used for creating/modifying database objects matches execute
    create, // used for DDL - CREATE operation using 'execute'
    drop, // used for DDL - DROP operation using 'execute'
    insert, // used for DML - INSERT operation using 'executeUpdate'
    update, // used for DML - UPDATE operation using 'executeUpdate'
    delete, // used for DML - DELETE operation using 'executeUpdate'
    select, // used for DML - SELECT operation using 'executeQuery'
    dml, // used for DML operations like SELECT|INSERT|UPDATE|DELETE leveraging `executeUpdate` & `executeQuery`
    ddl, // used for DDL operations like CREATE|DROP DATABASE|TABLE leveraging `execute`
}
