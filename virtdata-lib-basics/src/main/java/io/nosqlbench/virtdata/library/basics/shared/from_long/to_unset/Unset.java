package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.VALUE;

import java.util.function.LongFunction;

/**
 * Always yields the VALUE.unset value, which signals to
 * any consumers that the value provided should be considered
 * undefined for any operation. This is distinct from functions
 * which return a null, which is considered an actual value to
 * be acted upon.
 *
 * It is deemed an error for any downstream user of this library
 * to do anything with VALUE.unset besides explicitly acting like
 * it wasn't provided. That is the point of VALUE.unset.
 *
 * The purpose of having such a value in this library is to provide
 * a value type to help bridge between functional flows and imperative
 * run-times. Without such a value, it would be difficult to simulate
 * value streams in which some of the time values are set and other
 * times they are not.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class Unset implements LongFunction<Object> {
    @Override
    public Object apply(long value) {
        return VALUE.unset;
    }
}
