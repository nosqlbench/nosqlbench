package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveDouble implements LongToDoubleFunction {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"Save('foo')","save the current double value to the name 'foo' in this thread"})
    public SaveDouble(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"Save(NumberNameToString())","save a double value to a named variable in the current thread" +
            ", where the variable name is provided by a function."})
    public SaveDouble(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public double applyAsDouble(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        map.put(varname,value);
        return value;
    }
}
