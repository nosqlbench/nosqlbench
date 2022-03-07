package io.nosqlbench.datamappers.functions.geometry;

import com.datastax.dse.driver.internal.core.data.geometry.DefaultPoint;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Create a Point generator which generates com.datastax.driver.dse.geometry.Point
 * objects.
 */
@ThreadSafeMapper
@Categories({Category.objects})
public class Point implements LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point> {

    private final LongToDoubleFunction xfunc;
    private final LongToDoubleFunction yfunc;


    public Point(double x, double y) {
        this.xfunc = (u) -> x;
        this.yfunc = (v) -> y;
    }

    public Point(double x, LongToDoubleFunction yfunc) {
        this.xfunc = (u) -> x;
        this.yfunc = yfunc;
    }

    public Point(LongToDoubleFunction xfunc, double y) {
        this.xfunc = xfunc;
        this.yfunc = (v) -> y;
    }

    public Point(LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.xfunc = xfunc;
        this.yfunc = yfunc;
    }


    @Override
    public com.datastax.dse.driver.api.core.data.geometry.Point apply(long value) {
        return new DefaultPoint(xfunc.applyAsDouble(value), yfunc.applyAsDouble(value));
    }
}
