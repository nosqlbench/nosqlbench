package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Add the specified prefix String to the input value and return the result.
 */
@ThreadSafeMapper
public class Prefix implements Function<String,String>{
    private String prefix;

    @Example({"Prefix('PREFIX:')", "Prepend 'PREFIX:' to every input value"})
    public Prefix(String prefix){
        this.prefix = prefix;
    }

    @Override
    public String apply(String s) {
        return prefix + s;
    }
}
