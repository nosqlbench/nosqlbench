package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashedDoubleRange;

import java.util.function.Function;

/**
 * Reads a long variable from the thread local variable map, hashes and scales it
 * to the unit interval 0.0d - 1.0d, then uses the result to determine whether
 * to return a null object or the input value.
 */
@ThreadSafeMapper
@Categories({Category.state, Category.nulls})
public class NullOrPass implements Function<Object, Object> {

    private final String varname;
    private double ratio;
    private HashedDoubleRange rangefunc = new HashedDoubleRange(0.0D, 1.0D);

    public NullOrPass(double ratio, String varname) {
        if (ratio < 0.0D || ratio > 1.0D) {
            throw new RuntimeException("The " + NullOrPass.class.getSimpleName() + " function requires a ratio between 0.0D and 1.0D");
        }
        this.ratio = ratio;
        this.varname = varname;
    }

    @Override
    public Object apply(Object o) {
        long basis;
        Object o1 = SharedState.tl_ObjectMap.get().get(varname);
        if (o1 instanceof Long) {
            basis = (Long) o1;
        } else {
            throw new RuntimeException("The " + NullOrPass.class.getSimpleName() +
                " function requires a variable to have been saved with Save(" + varname + ")");
        }
        double v = rangefunc.applyAsDouble(basis);
        if (v <= ratio) {
            return null;
        }
        return o;
    }
}
