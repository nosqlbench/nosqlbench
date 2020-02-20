package io.virtdata.libbasics.shared.stateful;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Load a named value from the per-thread state map.
 * The previous input value will be forgotten, and the named value will replace it
 * before the next function in the chain.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Load implements Function<Object,Object> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final Object defaultValue;

    @Example({"Load('foo')","for the current thread, load an Object value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load an Object value from the named variable, where the variable name is returned by the provided function"})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=null;
    }

    @Example({"Load('foo','testvalue')","for the current thread, load an Object value from the named variable, " +
            "or the default value if the variable is not yet defined."})
    public Load(String name, Object defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"Load(NumberNameToString(),'testvalue')","for the current thread, load an Object value from the named " +
            "variable, where the variable name is returned by the provided function, or the" +
            "default value if the variable is not yet defined."})
    public Load(Function<Object,Object> nameFunc, Object defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return output;
    }

}
