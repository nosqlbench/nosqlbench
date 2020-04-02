package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.MVELExpr;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class Expr implements LongFunction<String> {

    private final String expr;
    private final Serializable compiledExpr;

    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(long.class, "cycle", expr);
    }

    @Override
    public String apply(long operand) {
        ConcurrentHashMap<String, Object> gl_map = SharedState.gl_ObjectMap;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();

        // merge gl into tl, for duplicates use the value from tl
        for (Map.Entry<String, Object> stringObjectEntry : gl_map.entrySet()) {
            map.merge(stringObjectEntry.getKey(), stringObjectEntry.getValue(), (entry1, entry2) -> entry1);
        }

        map.put("cycle",operand);
        String result = MVEL.executeExpression(compiledExpr, map, String.class);
        return result;
    }
}
