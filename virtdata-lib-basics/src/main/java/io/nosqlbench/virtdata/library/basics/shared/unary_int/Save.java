package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.function.Function;
import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
@Categories({Category.state})
public class Save implements IntUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"Save('foo')","save the current int value to the name 'foo' in this thread"})
    public Save(String name) {
        this.name = name;
        this.nameFunc = null;
    }

    @Example({"Save(NumberNameToString())","save the current int value to a named variable in this thread," +
            "where the variable name is provided by a function."})
    public Save(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc = nameFunc;
    }

    @Override
    public int applyAsInt(int operand) {
        String varname = (nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        SharedState.tl_ObjectMap.get().put(varname,operand);
        return operand;
    }
}
