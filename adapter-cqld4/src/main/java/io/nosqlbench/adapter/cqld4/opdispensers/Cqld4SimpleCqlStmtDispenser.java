package io.nosqlbench.adapter.cqld4.opdispensers;

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


import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import io.nosqlbench.adapter.cqld4.Cqld4Op;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4SimpleCqlStatement;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

public class Cqld4SimpleCqlStmtDispenser implements OpDispenser<Cqld4Op> {

    private final CqlSession session;
    private final ParsedOp cmd;
    private final int maxpages;
    private final boolean retryreplace;
    private final Cqld4OpMetrics metrics;

    public Cqld4SimpleCqlStmtDispenser(CqlSession session, ParsedOp cmd) {
        this.session = session;
        this.cmd = cmd;
        this.maxpages = cmd.getStaticConfigOr("maxpages",1);
        this.retryreplace = cmd.getStaticConfigOr("retryreplace",false);
        this.metrics = new Cqld4OpMetrics();
    }

    @Override
    public Cqld4SimpleCqlStatement apply(long value) {
        String stmtBody = cmd.get("stmt",value);
        SimpleStatement simpleStatement = SimpleStatement.newInstance(stmtBody);
        return new Cqld4SimpleCqlStatement(session,simpleStatement,maxpages,retryreplace,metrics);
    }
}
