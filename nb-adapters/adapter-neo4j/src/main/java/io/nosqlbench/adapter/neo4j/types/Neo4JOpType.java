/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.neo4j.types;

public enum Neo4JOpType {

    sync_autocommit("sync_autocommit"),

    async_autocommit("async_autocommit"),

    sync_read_transaction("sync_read_transaction"),

    async_read_transaction("async_read_transaction"),

    sync_write_transaction("sync_write_transaction"),

    async_write_transaction("async_write_transaction");

    private final String value;

    Neo4JOpType(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
