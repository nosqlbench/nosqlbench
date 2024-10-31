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

package io.nosqlbench.adapter.neo4j.ops;

import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.List;


public class Neo4JSyncAutoCommitOp extends Neo4JBaseOp {
    private final Session session;

    public Neo4JSyncAutoCommitOp(Session session, Query query) {
        super(query);
        this.session = session;
    }

    @Override
    public final Record[] apply(long value) {
        List<Record> recordList = session.run(query).list();
        if (session.isOpen()) {
            session.close();
        }
        return recordList.toArray(new Record[recordList.size()]);
    }
}
