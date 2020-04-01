package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.MVELExpr;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ThreadSafeMapper
public class Expr implements DoubleUnaryOperator {

    private final String expr;
    private final Serializable compiledExpr;

    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(double.class, "cycle", expr);
    }

    @Override
    public double applyAsDouble(double operand) {
        ConcurrentHashMap<String, Object> gl_map = SharedState.gl_ObjectMap;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();

        // merge gl into tl, for duplicates use the value from tl
        for (Map.Entry<String, Object> stringObjectEntry : gl_map.entrySet()) {
            map.merge(stringObjectEntry.getKey(), stringObjectEntry.getValue(), (entry1, entry2) -> entry1);
        }

        map.put("cycle",operand);
        double result = MVEL.executeExpression(compiledExpr, map, double.class);
        return result;
    }
}
