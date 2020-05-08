package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

import com.datastax.driver.dse.geometry.Point;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

@SuppressWarnings("ALL")
@ThreadSafeMapper
public class Polygon implements LongFunction<com.datastax.driver.dse.geometry.Polygon> {

    private final LongFunction<Point> pointfunc;
    private final LongToIntFunction lenfunc;

    public Polygon(LongToIntFunction lenfunc, LongFunction<Point> pointfunc) {
        this.pointfunc = pointfunc;
        this.lenfunc = lenfunc;
    }

    public Polygon(LongToIntFunction lenfunc, LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.lenfunc = lenfunc;
        this.pointfunc=new io.nosqlbench.activitytype.cql.datamappers.functions.geometry.Point(xfunc,yfunc);
    }

    public Polygon(int len, LongFunction<Point> pointfunc) {
        this.lenfunc = (i) -> len;
        this.pointfunc = pointfunc;
    }

    @Override
    public com.datastax.driver.dse.geometry.Polygon apply(long value) {
        int linelen = Math.max(lenfunc.applyAsInt(value),3);
        Point p0 = pointfunc.apply(value);
        Point p1 = pointfunc.apply(value+1);
        Point p2 = pointfunc.apply(value+2);
        Point[] points = new Point[linelen-3];

        for (int i = 3; i < linelen; i++) {
            points[i-3]=pointfunc.apply(value+i);
        }
        return new com.datastax.driver.dse.geometry.Polygon(p0,p1,p2,points);
    }
}
