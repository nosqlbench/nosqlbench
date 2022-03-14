/*
 * Copyright (c) 2022 nosqlbench
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
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.Statement;
import io.nosqlbench.adapter.cqld4.Cqld4OpMetrics;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public abstract class BaseCqlStmtDispenser extends BaseOpDispenser<Cqld4CqlOp> {

    private final int maxpages;
    private final Cqld4OpMetrics metrics = new Cqld4OpMetrics();
    private final LongFunction<CqlSession> sessionFunc;
    private final boolean isRetryReplace;

    public BaseCqlStmtDispenser(LongFunction<CqlSession> sessionFunc, ParsedOp op) {
        super(op);
        this.sessionFunc = sessionFunc;
        this.maxpages = op.getStaticConfigOr("maxpages",1);
        this.isRetryReplace = op.getStaticConfigOr("retryreplace",false);
    }

    public int getMaxPages() {
        return maxpages;
    }

    public boolean isRetryReplace() {
        return isRetryReplace;
    }

    public LongFunction<CqlSession> getSessionFunc() {
        return sessionFunc;
    }

    /**
     * All implementations of a CQL Statement Dispenser should be using the method
     * provided by this function. This ensures that {@link Statement}-level attributes
     * are handled uniformly and in one place.
     *
     * This takes the base statement function and decorates it optionally with each
     * additional qualified modifier, short-circuiting those which are not specified.
     * This allows default behavior to take precedence as well as avoids unnecessary calling
     * overhead for implicit attributes. This should be called when the stmt function is
     * initialized within each dispenser, not for each time dispensing occurs.
     */
    protected LongFunction<Statement> getEnhancedStmtFunc(LongFunction<Statement> basefunc, ParsedOp op) {
        LongFunction<Statement> partial = basefunc;
        partial = op.enhanceEnum(partial, "cl", DefaultConsistencyLevel.class, Statement::setConsistencyLevel);
        partial = op.enhanceEnum(partial, "scl", DefaultConsistencyLevel.class, Statement::setSerialConsistencyLevel);
        partial = op.enhance(partial, "idempotent", Boolean.class, Statement::setIdempotent);
        return partial;
    }


}
