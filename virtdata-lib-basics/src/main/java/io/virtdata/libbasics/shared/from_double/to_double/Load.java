package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@Categories(Category.state)
@ThreadSafeMapper
public class Load implements DoubleUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;
    private final double defaultValue;


    @Example({"Load('foo')","for the current thread, load a double value from the named variable"})
    public Load(String name) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=0.0D;
    }

    @Example({"Load('foo',432.0D)","for the current thread, load a double value from the named variable, or the default" +
            "value if it is not yet defined."})
    public Load(String name, double defaultValue) {
        this.name = name;
        this.nameFunc =null;
        this.defaultValue=defaultValue;
    }

    @Example({"Load(NumberNameToString())","for the current thread, load a double value from the named variable, where the variable" +
            "name is provided by a function."})
    public Load(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc =nameFunc;
        this.defaultValue=0.0D;
    }

    @Example({"Load(NumberNameToString(),1234.5D)","for the current thread, load a double value from the named variable, where the variable" +
            "name is provided by a function, or the default value if the named value is not yet defined."})
    public Load(Function<Object,Object> nameFunc, double defaultValue) {
        this.name = null;
        this.nameFunc =nameFunc;
        this.defaultValue=defaultValue;
    }

    @Override
    public double applyAsDouble(double operand) {
        String varname = nameFunc !=null ? String.valueOf(nameFunc.apply(operand)) : name;
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        Object o = map.getOrDefault(varname, defaultValue);
        return (double) o;
    }
}
