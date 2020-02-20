package io.virtdata.libbasics.shared.stateful;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.Function;

/**
 * Clears the per-thread map which is used by the Expr function.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Clear implements Function<Object,Object> {

    private final String[] names;

    /**
     * Clear all named entries from the per-thread map.
     */
    @Example({"Clear()","clear all thread-local variables"})
    public Clear() {
        this.names=null;
    }

    /**
     * Clear the specified names from the per-thread map.
     * @param names The names to be removed from the map.
     */
    @Example({"Clear('foo')","clear the thread-local variable 'foo'"})
    @Example({"Clear('foo','bar')","clear the thread-local variables 'foo' and 'bar'"})
    public Clear(String... names) {
        this.names = names;
    }

    @Override
    public Object apply(Object operand) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        if (names==null) {
            map.clear();
            return operand;
        }
        for (String name : names) {
            map.remove(name);
        }

        return operand;
    }
}
