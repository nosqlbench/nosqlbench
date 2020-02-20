package io.virtdata.libbasics.shared.from_double.to_float;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input double value to the closest float value.
 */
@ThreadSafeMapper
public class DoubleToFloat implements DoubleFunction<Float> {
    @Override
    public Float apply(double value) {
        return (float) value;
    }
}
