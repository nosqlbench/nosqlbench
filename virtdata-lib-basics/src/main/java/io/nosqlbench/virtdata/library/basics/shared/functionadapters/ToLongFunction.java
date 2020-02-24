package io.nosqlbench.virtdata.library.basics.shared.functionadapters;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.*;

/**
 * Adapts any compatible {@link FunctionalInterface} type to a LongFunction,
 * for use with higher-order functions, when they require a
 * LongFunction as an argument. Some of the higher-order functions within
 * this library specifically require a LongFunction as an argument, while
 * some of the other functions are provided in semantically equivalent
 * forms with compatible types which can't be converted directly or
 * automatically by Java.
 *
 * In such cases, those types of functions can be wrapped with the forms
 * described here in order to allow the inner and outer functions to work together.
 */
@ThreadSafeMapper
@Categories({Category.diagnostics})
public class ToLongFunction implements LongFunction<Object> {

    private LongFunction<?> function;

    public ToLongFunction(LongUnaryOperator op) {
        this.function = op::applyAsLong;
    }
    public ToLongFunction(Function<Long,Long> op) {
        this.function = op::apply;
    }
    public ToLongFunction(LongToIntFunction op) {
        this.function = op::applyAsInt;
    }
    public ToLongFunction(LongToDoubleFunction op) {
        this.function = op::applyAsDouble;
    }
    public ToLongFunction(LongFunction<?> func) {
        this.function = func;
    }


    @Override
    public Object apply(long value) {
        return function.apply(value);
    }
}
