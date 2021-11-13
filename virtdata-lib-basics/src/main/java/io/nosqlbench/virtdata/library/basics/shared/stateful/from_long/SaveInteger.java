package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.function.LongFunction;
import java.util.function.LongToIntFunction;

@Categories(Category.state)
@ThreadSafeMapper
public class SaveInteger implements LongToIntFunction {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"SaveInteger('foo')","save the current integer value to a named variable in this thread."})
    public SaveInteger(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveInteger(NumberNameToString())","save the current integer value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveInteger(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public int applyAsInt(long value) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        SharedState.tl_ObjectMap.get().put(varname, value);
        return (int) value;
    }
}
