package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

/**
 * Load a value from a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * If the named variable is not defined, then the default value is returned.
 */
@Categories(Category.state)
@ThreadSafeMapper
public class LoadString implements LongFunction<String> {

    private final String name;
    private final LongFunction<Object> nameFunc;
    private final String defaultValue;

    @Example({"LoadString('foo','examplevalue')","for the current thread, load a String value from the named variable."})
    public LoadString(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue="";
    }

    @Example({"LoadString('foo','examplevalue')","for the current thread, load a String value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadString(String name, String defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"LoadString(NumberNameToString(),'examplevalue')","for the current thread, load a String value from the named variable," +
            " or the default value if the named variable is not defined."})
    public LoadString(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue="";
    }

    @Example({"LoadString(NumberNameToString(),'examplevalue')","for the current thread, load a String value from the named variable," +
            "where the variable name is provided by a function, or the default value if the named" +
            "variable is not defined."})
    public LoadString(LongFunction<Object> nameFunc, String defaultValue) {
        this.name=null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public String apply(long value) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object loaded = map.getOrDefault(varname,defaultValue);
        return (String) loaded;
    }
}
