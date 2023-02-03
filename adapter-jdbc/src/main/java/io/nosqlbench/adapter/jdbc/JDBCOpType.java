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
    select, // used for SELECT operation matches executeQuery
    update, // used for performing updates such as INSERT/UPDATE/DELETE matches executeUpdate
    ddl, // used for creating/modifying database objects matches execute
    create, // used for CREATE operation matches execute
    drop, // used for DROP operation matches execute
}
