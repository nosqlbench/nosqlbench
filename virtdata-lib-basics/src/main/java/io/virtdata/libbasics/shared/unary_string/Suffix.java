package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Add the specified prefix String to the input value and return the result.
 */
@ThreadSafeMapper
public class Suffix implements Function<String,String> {
    private String suffix;

    @Example({"Suffix('--Fin')", "Append '--Fin' to every input value"})
    public Suffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String apply(String s) {
        return s + suffix;
    }
}
