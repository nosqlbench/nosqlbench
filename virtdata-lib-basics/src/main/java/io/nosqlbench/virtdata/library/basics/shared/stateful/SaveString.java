package io.nosqlbench.virtdata.library.basics.shared.stateful;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveString implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"SaveString('foo')","save the current String value to a named variable in this thread."})
    public SaveString(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveString(NumberNameToString())","save the current String value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveString(Function<Object,Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public String apply(String s) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(s)) : name;
        map.put(varname,s);
        return s;
    }
}
