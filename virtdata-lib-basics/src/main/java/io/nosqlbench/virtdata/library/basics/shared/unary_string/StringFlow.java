package io.nosqlbench.virtdata.library.basics.shared.unary_string;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Combine multiple String functions together into one function.
 */
@Categories(Category.functional)
@ThreadSafeMapper
public class StringFlow implements Function<String,String> {

    private final Function<String, String>[] funcs;

    public StringFlow(Function<String,String>... funcs) {
        this.funcs = funcs;
    }

    @Override
    public String apply(String s) {
        String value = s;
        for (Function<String, String> func : funcs) {
            value = func.apply(value);
        }
        return value;
    }
}
