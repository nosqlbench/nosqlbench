package io.nosqlbench.virtdata.library.basics.shared.statistics;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataFunctions;

import java.util.DoubleSummaryStatistics;
import java.util.Locale;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

/**
 * Provide a moving aggregate (min,max,avg,sum,count) of long values presented.
 * This allows for sanity checks on values during prototyping, primarily.
 * Using it for other purposes in actual workloads is not generally desirable,
 * as this does not produce purely functional (deterministic) values.
 */
@ThreadSafeMapper
@Categories({Category.statistics})
public class LongStats implements DoubleUnaryOperator {

    DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
    DoubleSupplier accessor;
    DoubleUnaryOperator valueFunc;

    /**
     * Given the specified statistic, provide an
     * updated result for all the values presented to this function's input.
     * @param spec One of 'min', 'max', 'count', 'avg', or 'sum'
     */
    public LongStats(String spec) {
        this(spec,(DoubleUnaryOperator) v->v, true);
    }

    /**
     * Given the specified statistic, a function, and whether to allow truncating conversions,
     * provide an updated result for all the values produced by the provided function when
     * given the input.
     * @param spec One of 'min', 'max', 'count', 'avg', or 'sum'
     * @param func Any function which can take a long or compatible input and produce a numeric value
     * @param truncate Whether or not to allow truncating conversions (long to int for example)
     */
    public LongStats(String spec,Object func, boolean truncate) {
        switch (spec.toLowerCase(Locale.ROOT)) {
            case "min":
                accessor = stats::getMin;
                break;
            case "max":
                accessor = stats::getMax;
                break;
            case "average":
            case "avg":
                accessor = stats::getAverage;
                break;
            case "count":
                accessor = stats::getCount;
                break;
            case "sum":
                accessor = stats::getSum;
                break;
            default:
                throw new RuntimeException("You must specify one of min,max,avg,count,sum");
        }
        valueFunc= VirtDataFunctions.adapt(func,DoubleUnaryOperator.class,double.class,truncate);
    }

    @Override
    public double applyAsDouble(double operand) {
        stats.accept(valueFunc.applyAsDouble(operand));
        return accessor.getAsDouble();
    }
}
