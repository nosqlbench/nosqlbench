package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Converts the input to the most obvious string representation with String.valueOf(...).
 * Forms which accept a function will evaluate that function first and then apply
 * String.valueOf() to the result.
 */
@ThreadSafeMapper
public class ToString implements Function<Object,String> {

    @Override
    public String apply(Object o) {
        return String.valueOf(o);
    }
}
