/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.neo4j.ops;

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;

import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.async.AsyncSession;


public abstract class Neo4JBaseOp implements CycleOp<Record[]> {
    protected final Query query;

    public Neo4JBaseOp(Query query) {
        this.query = query;
    }

    /**
     * In the child classes, this method will be responsible for:
     * - using the Neo4J Session/AsyncSession object to run the Neo4J Query
     * - process the Result to get an array of Records
     * - close the Session/AsyncSession
     * - Return the array of Records
     *
     * Session creation and closing is considered light-weight. Reference:
     * - https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/Session.html#close()
     */
    public abstract Record[] apply(long value);

    @Override
    public String toString() {
        return "Neo4JBaseOp(" + query.toString().getClass().getSimpleName() + ")";
    }
}
