package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongUnaryOperator;

/**
 * Clears the per-thread map which is used by the Expr function.
 */
@ThreadSafeMapper
@Categories({Category.state})
public class Clear implements LongUnaryOperator {

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
    public long applyAsLong(long operand) {

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
