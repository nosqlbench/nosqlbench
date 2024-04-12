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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.adapter.cqld4.optionhelpers.BatchTypeEnum;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlBatchStatement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;

public class CqlD4BatchStmtDispenser extends Cqld4BaseOpDispenser {
    private final int repeat;
    private final ParsedOp subop;
    private final OpMapper submapper;
    private LongFunction<Statement> opfunc;

    public CqlD4BatchStmtDispenser(
        DriverAdapter adapter,
        LongFunction<CqlSession> sessionFunc,
        ParsedOp op,
        int repeat,
        ParsedOp subop,
        OpDispenser<? extends Cqld4CqlOp> subopDispenser
    ) {
        super(adapter, sessionFunc, op);
        this.repeat = repeat;
        this.subop = subop;
        this.opfunc = createStmtFunc(op, subopDispenser);
        this.submapper = adapter.getOpMapper();
        subopDispenser = submapper.apply(subop);

    }

    private LongFunction<Statement> createStmtFunc(ParsedOp topOp, OpDispenser<? extends Cqld4CqlOp> subopDispenser) {
        Cqld4CqlOp exampleOp = subopDispenser.apply(0L);
        Statement<?> example = exampleOp.getStmt();
        if (!(example instanceof BatchableStatement<?> b)) {
            throw new RuntimeException("Statement type '" + example.getClass().getCanonicalName() + " is not " +
                "batchable. query=" + exampleOp.getQueryString());
        }
        BatchTypeEnum bte = topOp.getEnumFromFieldOr(BatchTypeEnum.class, BatchTypeEnum.unlogged, "batchtype");
        LongFunction<BatchStatementBuilder> bsbf = l -> new BatchStatementBuilder(bte.batchtype);
        LongFunction<Statement> bsf = getBatchAccumulator(bsbf, subopDispenser);
        bsf = getEnhancedStmtFunc(bsf,topOp);
        return bsf;
    }

    @NotNull
    private LongFunction<Statement> getBatchAccumulator(LongFunction<BatchStatementBuilder> bsb, OpDispenser<? extends Cqld4CqlOp> subopDispenser) {
        LongFunction<BatchStatementBuilder> f = l -> {
            BatchStatementBuilder bsa = bsb.apply(l);
            for (int i = 0; i < repeat; i++) {
                Cqld4CqlOp op = subopDispenser.apply(i+l);
                BatchableStatement<?> stmt = (BatchableStatement<?>) op.getStmt();
                bsa= bsa.addStatement(stmt);
            }
            return bsa;
        };

        LongFunction<Statement> bsf = (long l) -> f.apply(l).build();
        return bsf;
    }

    @Override
    public Cqld4CqlOp getOp(long value) {
        Statement bstmt = opfunc.apply(value);
        return new Cqld4CqlBatchStatement(
            getSessionFunc().apply(value),
            (BatchStatement) bstmt,
            getMaxPages(),
            getMaxLwtRetries(),
            isRetryReplace(),
            this
        );
    }
}
