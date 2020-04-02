package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@Categories(Category.state)
@ThreadSafeMapper
public class LoadInteger implements Function<Object,Integer> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final int defaultValue;

    @Example({"LoadInteger('foo')","for the current thread, load an integer value from the named variable."})
    public LoadInteger(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=0;
    }

    @Example({"LoadInteger('foo',42)","for the current thread, load an integer value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadInteger(String name, int defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue = defaultValue;
    }

    @Example({"LoadInteger(NumberNameToString())","for the current thread, load an integer value from the named variable," +
            "where the variable name is provided by a function."})
    public LoadInteger(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        defaultValue = 0;
    }

    @Example({"LoadInteger(NumberNameToString(),42)","for the current thread, load an integer value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadInteger(Function<Object,Object> nameFunc, int defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Integer apply(Object o) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        Object value = map.getOrDefault(varname, defaultValue);
        return (Integer) value;
    }
}
