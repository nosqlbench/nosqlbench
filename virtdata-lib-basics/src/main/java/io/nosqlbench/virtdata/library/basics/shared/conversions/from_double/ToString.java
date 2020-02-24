package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

@Categories({Category.conversion})
@ThreadSafeMapper
public class ToString implements DoubleFunction<String> {
    private final DoubleUnaryOperator doubleFunc;

    @Example({"ToString()","map the double input value to a String"})
    public ToString() {
        doubleFunc = (d) -> d;
    }

    @Example({"ToString(Add(5.7D))","map the double input value X to X+5.7D and then to a String"})
    public ToString(DoubleUnaryOperator df) {
        this.doubleFunc = df;
    }

    public ToString(DoubleFunction<Double> df) {
        this.doubleFunc = df::apply;
    }

    public ToString(Function<Double,Double> df) {
        this.doubleFunc = df::apply;
    }

    public String apply(double v) {
        return String.valueOf(doubleFunc.applyAsDouble(v));
    }
}
