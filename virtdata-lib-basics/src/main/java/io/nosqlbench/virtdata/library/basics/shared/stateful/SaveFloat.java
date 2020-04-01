package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveFloat implements UnaryOperator<Float> {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"SaveFloat('foo')","save the current float value to a named variable in this thread."})
    public SaveFloat(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveFloat(NumberNameToString())","save the current float value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveFloat(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public Float apply(Float operand) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(operand)) : name;
        map.put(varname,operand);
        return operand;
    }
}
