package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

@Categories(Category.state)
@ThreadSafeMapper
public class LoadFloat implements LongFunction<Float> {

    private final String name;
    private final LongFunction<Object> nameFunc;
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
    public LoadFloat(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue = 0.0F;
    }

    @Example({"LoadFloat(NumberNameToString(),23F)","for the current thread, load a float value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadFloat(LongFunction<Object> nameFunc, float defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue = defaultValue;
    }

    @Override
    public Float apply(long value) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object loaded = map.getOrDefault(varname,defaultValue);
        return (Float) loaded;
    }
}
