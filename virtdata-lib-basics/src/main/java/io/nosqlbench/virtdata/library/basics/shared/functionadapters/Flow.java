package io.nosqlbench.virtdata.library.basics.shared.functionadapters;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.composers.FunctionAssembly;
import io.nosqlbench.virtdata.api.composers.FunctionComposer;
import io.nosqlbench.virtdata.api.ResolvedFunction;
import io.nosqlbench.virtdata.api.VirtDataFunctions;

import java.util.function.LongFunction;

/**
 * <p>Combine functions into one.</p>
 *
 * <p>This function allows you to combine multiple other functions into one. This is often useful
 * for constructing more sophisticated recipes, when you don't have the ability to use
 * control flow or non-functional forms.</p>
 *
 * <p>The functions will be stitched together using the same logic that VirtData uses when
 * combining flows outside functions. That said, if the functions selected are not the right ones,
 * then it is possible to end up with the wrong data type at the end. To remedy this, be sure
 * to add input and output qualifiers, like <code>long-&gt;</code> or <code>-&gt;String</code> where
 * appropriate, to ensure that VirtData selects the right functions within the flow.</p>
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class Flow implements LongFunction<Object> {

    private final LongFunction f;

    public Flow(Object... funcs) {
        FunctionComposer assembly = new FunctionAssembly();
        for (Object func : funcs) {
            assembly = assembly.andThen(func);
        }
        ResolvedFunction rf = assembly.getResolvedFunction();
        Object functionObject = rf.getFunctionObject();
        f = VirtDataFunctions.adapt(functionObject,LongFunction.class, Object.class, true);
//        f = LongFunction.class.cast(functionObject);
    }

    @Override
    public Object apply(long value) {
        Object o = f.apply(value);
        return o;
    }
}
