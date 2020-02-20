package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.state})
public class Save implements Function<String,String> {

    private final String name;
    private final Function<Object,Object> nameFunc;

    @Example({"Save('foo')","save the current String value to the name 'foo' in this thread"})
    public Save(String name) {
        this.name = name;
        this.nameFunc=null;
    }

    @Example({"Save(NumberNameToString())","save the current String value to a named variable in" +
            " this thread, where the variable name is provided by a function"})
    public Save(Function<Object,Object> nameFunc) {
        this.name = null;
        this.nameFunc=nameFunc;
    }

    @Override
    public String apply(String s) {
        SharedState.tl_ObjectMap.get().put(name,s);
        return s;
    }

}
