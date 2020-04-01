package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

/**
 * Save the current input value at this point in the function chain to a thread-local variable name.
 * The input value is unchanged, and available for the next function in the chain to use as-is.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Save implements LongUnaryOperator {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"Save('foo')","for the current thread, save the input object value to the named variable"})
    public Save(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"Save(NumberNameToString())","for the current thread, save the current input object value to the named variable," +
            "where the variable name is provided by a function."})
    public Save(LongFunction<Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
    }

    @Override
    public long applyAsLong(long operand) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        map.put(varname,operand);
        return operand;
    }
}
