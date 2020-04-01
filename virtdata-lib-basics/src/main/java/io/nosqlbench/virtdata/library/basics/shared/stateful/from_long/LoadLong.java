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
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@Categories(Category.state)
@ThreadSafeMapper
public class LoadLong implements LongUnaryOperator {

    private final String name;
    private final LongFunction<Object> nameFunc;
    private long defaultValue;

    @Example({"LoadLong('foo',42L)","for the current thread, load a long value from the named variable."})
    public LoadLong(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=0L;
    }

    @Example({"LoadLong('foo',42L)","for the current thread, load a long value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadLong(String name, long defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"LoadLong(NumberNameToString(),42L)","for the current thread, load a long value from the named variable," +
            "where the variable name is provided by a function."})
    public LoadLong(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=0L;
    }

    @Example({"LoadLong(NumberNameToString(),42L)","for the current thread, load a long value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadLong(LongFunction<Object> nameFunc, long defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public long applyAsLong(long operand) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object loaded = map.getOrDefault(varname,defaultValue);
        return (Long) loaded;
    }
}
