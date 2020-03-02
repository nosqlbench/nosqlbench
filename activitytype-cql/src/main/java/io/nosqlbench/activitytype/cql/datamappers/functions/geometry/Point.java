package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Create a Point generator which generates com.datastax.driver.dse.geometry.Point
 * objects.
 */
@ThreadSafeMapper
public class Point implements LongFunction<com.datastax.driver.dse.geometry.Point> {

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
    public com.datastax.driver.dse.geometry.Point apply(long value) {
        return new com.datastax.driver.dse.geometry.Point(xfunc.applyAsDouble(value), yfunc.applyAsDouble(value));
    }
}
