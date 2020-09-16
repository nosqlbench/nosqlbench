package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.function.Function;
import java.util.function.LongUnaryOperator;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * SaveLong(Load('id'))
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveLong implements LongUnaryOperator {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"SaveLong('foo')","save the current long value to a named variable in this thread."})
    public SaveLong(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveLong(NumberNameToString())","save the current long value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveLong(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public long applyAsLong(long operand) {
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        SharedState.tl_ObjectMap.get().put(varname, operand);
        return operand;
    }
}
