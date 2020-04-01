package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

import com.datastax.driver.dse.geometry.Point;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;

/**
 * Create a Distance generator which produces
 * com.datastax.driver.dse.geometry.Distance objects.
 */
@ThreadSafeMapper
public class Distance implements LongFunction<com.datastax.driver.dse.geometry.Distance> {

    private final io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point pointfunc;
    private final LongToDoubleFunction rfunc;

    public Distance(LongToDoubleFunction xfunc, LongToDoubleFunction yfunc, LongToDoubleFunction rfunc) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,yfunc);
        this.rfunc = rfunc;
    }

    public Distance(double x, LongToDoubleFunction yfunc, LongToDoubleFunction rfunc) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point((u)->x,yfunc);
        this.rfunc = rfunc;
    }

    public Distance(LongToDoubleFunction xfunc, double y, LongToDoubleFunction rfunc) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,(v)->y);
        this.rfunc = rfunc;
    }

    public Distance(double x, double y, LongToDoubleFunction rfunc) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point((u)->x,(v)->y);
        this.rfunc = rfunc;
    }

    public Distance(LongToDoubleFunction xfunc, LongToDoubleFunction yfunc, double r) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,yfunc);
        this.rfunc = (w) -> r;
    }

    public Distance(double x, LongToDoubleFunction yfunc, double r) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point((u)->x,yfunc);
        this.rfunc = (w) -> r;
    }

    public Distance(LongToDoubleFunction xfunc, double y, double r) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,(v)->y);
        this.rfunc = (w) -> r;
    }

    public Distance(double x, double y, double r) {
        pointfunc = new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point((u) -> x, (v) -> y);
        this.rfunc = (w) -> r;
    }


    @Override
    public com.datastax.driver.dse.geometry.Distance apply(long value) {
        Point apoint = pointfunc.apply(value);
        double aradius = rfunc.applyAsDouble(value);
        return new com.datastax.driver.dse.geometry.Distance(apoint,aradius);
    }
}
