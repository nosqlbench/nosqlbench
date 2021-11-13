package io.nosqlbench.virtdata.library.basics.shared.formatting;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Formats a floating point value to a string using the java.text.DecimalFormat
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class DecimalFormat implements DoubleFunction<String> {
    private final java.text.DecimalFormat format;

    @Example({"DecimalFormat('.##')","converts a double value to a string with only two digits after the decimal"})
    public DecimalFormat(String format) {
        this.format = new java.text.DecimalFormat(format);
    }

    @Override
    public String apply(double value) {
        return this.format.format(value);
    }
}
