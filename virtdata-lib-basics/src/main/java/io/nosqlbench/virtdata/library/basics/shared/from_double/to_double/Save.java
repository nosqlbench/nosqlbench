package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@Categories(Category.state)
@ThreadSafeMapper
public class Save implements DoubleUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"Save('foo')","for the current thread, save the current double value to the named variable."})
    public Save(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"Save(NumberNameToString())","for the current thread, save the current double value to the name 'foo' in this thread" +
            ", where the variable name is provided by a function."})
    public Save(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc=nameFunc;
    }

    @Override
    public double applyAsDouble(double operand) {
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        SharedState.tl_ObjectMap.get().put(varname, operand);
        return operand;
    }
}
