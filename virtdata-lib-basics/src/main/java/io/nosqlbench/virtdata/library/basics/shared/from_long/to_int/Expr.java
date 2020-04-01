package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.MVELExpr;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.LongToIntFunction;

/**
 * Allow for the use of arbitrary expressions according to the
 * <a href="http://mvel.documentnode.com/">MVEL</a> expression language.
 *
 * Variables that have been set by a Save function are available
 * to be used in this function.
 *
 * The variable name <code>cycle</code> is reserved, and is always equal to
 * the current input value. This is not the same in every case as the
 * current cycle of an operation. It could be different if there
 * are preceding functions which modify the input value.
 */
@ThreadSafeMapper
public class Expr implements LongToIntFunction {

    private final String expr;
    private final Serializable compiledExpr;


    public Expr(String expr) {
        this.expr = expr;
        this.compiledExpr = MVELExpr.compile(long.class, "cycle", expr);
    }

    @Override
    public int applyAsInt(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        map.put("cycle",value);
        int result = MVEL.executeExpression(compiledExpr, map, int.class);
        return result;
    }
}
