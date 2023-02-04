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
 * Op templates which are supported by the NoSQLBench JDBC driver are
 * enumerated below. These command names should mirror those in the official
 * CockroachDB API exactly, as an example. See the official API for more details.
 *
 * @see <a href="https://www.cockroachlabs.com/docs/v22.2/sql-statements.html#data-definition-statements">CockroachDB API Reference</a>
 */
public enum JDBCOpType {
    //See https://jdbc.postgresql.org/documentation/query/
    jdbcQuery,
}
