package io.virtdata.libbasics.shared.stateful.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

/**
 * Save a value to a named thread-local variable, where the variable
 * name is fixed or a generated variable name from a provided function.
 * Note that the input type is not that suitable for constructing names,
 * so this is more likely to be used in an indirect naming pattern like
 * <pre>SaveDouble(Load('id'))</pre>
 */
@Categories(Category.state)
@ThreadSafeMapper
public class SaveString implements LongFunction<String> {

    private final String name;
    private final LongFunction<Object> nameFunc;

    @Example({"SaveString('foo')","save the current String value to a named variable in this thread."})
    public SaveString(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"SaveString(NumberNameToString())","save the current String value to a named variable in this thread" +
            ", where the variable name is provided by a function."})
    public SaveString(LongFunction<Object> nameFunc) {
        this.name=null;
        this.nameFunc = nameFunc;
    }

    @Override
    public String apply(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;
        map.put(varname,value);
        return String.valueOf(value);
    }
}
