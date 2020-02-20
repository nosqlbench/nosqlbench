package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.MVELExpr;
import io.virtdata.libbasics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.DoubleUnaryOperator;

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
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        map.put("cycle",operand);
        double result = MVEL.executeExpression(compiledExpr, map, double.class);
        return result;
    }
}
