package io.virtdata.libbasics.shared.stateful;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@Categories(Category.state)
@ThreadSafeMapper
public class LoadFloat implements Function<Object,Float> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final float defaultValue;


    @Example({"LoadFloat('foo')","for the current thread, load a float value from the named variable."})
    public LoadFloat(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue = 0.0F;
    }

    @Example({"LoadFloat('foo',23F)","for the current thread, load a float value from the named variable," +
            "or the default value if the named variable is not defined."})
    public LoadFloat(String name, float defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue = defaultValue;
    }

    @Example({"LoadFloat(NumberNameToString())","for the current thread, load a float value from the named variable," +
            "where the variable name is provided by a function."})
    public LoadFloat(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue = 0.0F;
    }

    @Example({"LoadFloat(NumberNameToString(),23F)","for the current thread, load a float value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadFloat(Function<Object,Object> nameFunc, float defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Float apply(Object o) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(o)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object value = map.getOrDefault(varname,defaultValue);
        return (Float) value;
    }
}
