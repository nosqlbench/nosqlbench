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

package io.nosqlbench.adapter.cqld4.opdispensers;

import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.adapter.cqld4.Cqld4DriverAdapter;
import io.nosqlbench.adapter.cqld4.optionhelpers.BatchTypeEnum;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlBatchStatement;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongFunction;

public class CqlD4BatchStmtDispenser extends Cqld4CqlBaseOpDispenser<Cqld4CqlBatchStatement> {
    private final int repeat;
    private final ParsedOp subop;
    private final OpMapper submapper;
    private LongFunction<BatchStatement> opfunc;

    public CqlD4BatchStmtDispenser(
        Cqld4DriverAdapter adapter,
        ParsedOp op,
        int repeat,
        ParsedOp subop,
        OpDispenser<Cqld4CqlOp> subopDispenser
    ) {
        super(adapter, op);
        this.repeat = repeat;
        this.subop = subop;
        this.opfunc = createStmtFunc(op, subopDispenser);
        this.submapper = adapter.getOpMapper();
        subopDispenser = submapper.apply(this, subop, adapter.getSpaceFunc(op));

    }

    private LongFunction<BatchStatement> createStmtFunc(ParsedOp topOp, OpDispenser<? extends Cqld4CqlOp> subopDispenser) {
        Cqld4CqlOp exampleOp = subopDispenser.apply(0L);
        Statement<?> example = exampleOp.getStmt();
        if (!(example instanceof BatchableStatement<?> b)) {
            throw new RuntimeException("Statement type '" + example.getClass().getCanonicalName() + " is not " +
                "batchable. query=" + exampleOp.getQueryString());
        }
        BatchTypeEnum bte = topOp.getEnumFromFieldOr(BatchTypeEnum.class, BatchTypeEnum.unlogged, "batchtype");
        LongFunction<BatchStatementBuilder> bsbf = l -> new BatchStatementBuilder(bte.batchtype);
        LongFunction<BatchStatement> bsf = getBatchAccumulator(bsbf, subopDispenser);
        bsf = getEnhancedStmtFunc(bsf, topOp);
        return bsf;
    }

    @NotNull
    private LongFunction<BatchStatement> getBatchAccumulator(LongFunction<BatchStatementBuilder> bsb, OpDispenser<?
        extends Cqld4CqlOp> subopDispenser) {
        LongFunction<BatchStatementBuilder> f = l -> {
            long base = l * repeat;
            BatchStatementBuilder bsa = bsb.apply(l);
            for (int i = 0; i < repeat; i++) {
                Cqld4CqlOp op = subopDispenser.apply(base + i);
                BatchableStatement<?> stmt = (BatchableStatement<?>) op.getStmt();
                bsa = bsa.addStatement(stmt);
            }
            return bsa;
        };

        LongFunction<BatchStatement> bsf = (long l) -> f.apply(l).build();
        return bsf;
    }

    @Override
    public Cqld4CqlBatchStatement getOp(long cycle) {
        Statement bstmt = opfunc.apply(cycle);
        return new Cqld4CqlBatchStatement(
            sessionF.apply(cycle),
            (BatchStatement) bstmt,
            getMaxPages(),
            getMaxLwtRetries(),
            isRetryReplace(),
            this
        );
    }
}
