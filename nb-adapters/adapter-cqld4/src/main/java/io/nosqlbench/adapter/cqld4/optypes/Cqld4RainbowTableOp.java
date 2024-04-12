/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.cqld4.optypes;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.*;

import java.util.Map;

// Need to create RainbowTableStatement
public class Cqld4RainbowTableOp implements CycleOp<ResultSet>, VariableCapture, OpGenerator, OpResultSize {
//    private final CqlSession session;
//    private final RainbowTableStatement stmt;

    // Rename ResultSet to something more appropriate
    public final ResultSet apply(long cycle) {
        throw new RuntimeException("implement me");
        // TODO: actually write to sstables
        // sstable passed to shared memory object
    }

    @Override
    public Op getNextOp() {
        throw new RuntimeException("implement me");
    }

    @Override
    public Map<String, ?> capture() {
        throw new RuntimeException("implement me");
   }
//
//    public Cqld4RainbowTableOp(CqlSession session, RainbowTableStatement stmt, int maxpages, boolean retryreplace) {
//        //
//    }

}
