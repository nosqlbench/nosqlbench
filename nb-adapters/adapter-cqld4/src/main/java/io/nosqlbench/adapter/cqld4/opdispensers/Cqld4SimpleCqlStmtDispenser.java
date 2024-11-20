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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.Cqld4Space;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlSimpleStatement;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class Cqld4SimpleCqlStmtDispenser extends Cqld4CqlBaseOpDispenser<Cqld4CqlSimpleStatement> {

    private final LongFunction<Statement> stmtFunc;
    private final LongFunction<String> targetFunction;

    public Cqld4SimpleCqlStmtDispenser(Cqld4DriverAdapter adapter, LongFunction<String> targetFunction, ParsedOp cmd) {
        super(adapter, cmd);
        this.targetFunction=targetFunction;
        this.stmtFunc =createStmtFunc(cmd);
    }

    protected LongFunction<Statement> createStmtFunc(ParsedOp op) {
        return super.getEnhancedStmtFunc(l -> SimpleStatement.newInstance(targetFunction.apply(l)),op);
    }

    @Override
    public Cqld4CqlSimpleStatement getOp(long cycle) {
        return new Cqld4CqlSimpleStatement(
            this.sessionF.apply(cycle),
            (SimpleStatement) stmtFunc.apply(cycle),
            getMaxPages(),
            isRetryReplace(),
            getMaxLwtRetries(),
            this
        );
    }

}
