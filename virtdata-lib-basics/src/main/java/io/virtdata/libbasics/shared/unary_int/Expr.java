package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.MVELExpr;
import io.virtdata.libbasics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class Expr implements IntUnaryOperator {

    private final String expr;
    private final Serializable compiledExpr;

    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(int.class, "cycle", expr);
    }

    @Override
    public int applyAsInt(int operand) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        map.put("cycle",operand);
        int result = MVEL.executeExpression(compiledExpr, map, int.class);
        return result;
    }
}
