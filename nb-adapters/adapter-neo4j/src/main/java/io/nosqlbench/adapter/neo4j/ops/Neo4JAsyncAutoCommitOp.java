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

import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.async.AsyncSession;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Neo4JAsyncAutoCommitOp extends Neo4JBaseOp {
    private final AsyncSession session;

    public Neo4JAsyncAutoCommitOp(AsyncSession session, Query query) {
        super(query);
        this.session = session;
    }

    /**
     * Reference:
     * - https://neo4j.com/docs/api/java-driver/current/org.neo4j.driver/org/neo4j/driver/async/AsyncSession.html#runAsync(java.lang.String,java.util.Map,org.neo4j.driver.TransactionConfig)
     */
    @Override
    public final Record[] apply(long value) {
        try {
            CompletionStage<List<Record>> resultStage = session.runAsync(query).thenComposeAsync(
                cursor -> cursor.listAsync().whenComplete(
                    (records, throwable) -> {
                        if (throwable != null) {
                            session.closeAsync();
                        }
                    }
                )
            );
            List<Record> recordList = resultStage.toCompletableFuture().get(300, TimeUnit.SECONDS);
            return recordList.toArray(new Record[recordList.size()]);
        } catch (ExecutionException exe) {
            Throwable ee = exe.getCause();
            if (ee instanceof RuntimeException re) {
                throw re;
            } else throw new NBExecutionException(exe);
        } catch (InterruptedException ie) {
            throw new NBInterruptedException(ie);
        } catch (TimeoutException e) {
            throw new NBTimeoutException(e);
        }
    }
}
