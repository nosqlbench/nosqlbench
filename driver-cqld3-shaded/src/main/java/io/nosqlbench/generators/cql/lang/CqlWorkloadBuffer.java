package io.nosqlbench.generators.cql.lang;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
