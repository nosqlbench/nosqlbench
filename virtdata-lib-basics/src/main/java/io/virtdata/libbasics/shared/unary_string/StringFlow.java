package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
