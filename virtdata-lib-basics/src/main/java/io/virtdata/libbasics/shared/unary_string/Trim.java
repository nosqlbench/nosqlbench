package io.virtdata.libbasics.shared.unary_string;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Trim the input value and return the result.
 */
@ThreadSafeMapper
public class Trim implements Function<String, String>{

    @Override
    public String apply(String s) {
        return s.trim();
    }
}
