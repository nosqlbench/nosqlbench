package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

@Categories(Category.state)
@ThreadSafeMapper
public class Load implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final String defaultValue;


    @Example({"Load('foo')","for the current thread, load a String value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=null;
    }

    @Example({"Load('foo','track05')","for the current thread, load a String value from the named variable, or teh default value if the variable is not yet defined."})
    public Load(String name, String defaultvalue) {
        this.name = name;
        this.nameFunc = null;
        this.defaultValue=defaultvalue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load a String value from the named variable, where the variable name is provided by a function"})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=null;
    }

    @Example({"Load(NumberNameToString(),'track05')","for the current thread, load a String value from the named variable, where the variable name is provided by a function, or the default value if the variable is not yet defined."})
    public Load(Function<Object,Object> nameFunc, String defaultValue) {
        this.name = null;
        this.nameFunc = nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public String apply(String s) {
        String varname = nameFunc !=null ? String.valueOf(nameFunc.apply(s)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object output = map.getOrDefault(varname,defaultValue);
        return (String) output;
    }

}
