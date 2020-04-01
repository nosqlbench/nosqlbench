package io.nosqlbench.activitytype.cql.datamappers.functions.geometry;

//import com.datastax.driver.dse.geometry.Point;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

@SuppressWarnings("Duplicates")
@ThreadSafeMapper
public class LineString implements LongFunction<com.datastax.driver.dse.geometry.LineString> {

    private final LongFunction<com.datastax.driver.dse.geometry.Point> pointfunc;
    private final LongToIntFunction lenfunc;

    public LineString(LongToIntFunction lenfunc, LongFunction<com.datastax.driver.dse.geometry.Point> pointfunc) {
        this.pointfunc = pointfunc;
        this.lenfunc = lenfunc;
    }

    public LineString(LongToIntFunction lenfunc, LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.lenfunc = lenfunc;
        this.pointfunc=new Point(xfunc,yfunc);
    }

    public LineString(int len, LongFunction<com.datastax.driver.dse.geometry.Point> pointfunc) {
        this.lenfunc = (i) -> len;
        this.pointfunc = pointfunc;
    }

    @Override
    public com.datastax.driver.dse.geometry.LineString apply(long value) {
        int linelen = Math.max(lenfunc.applyAsInt(value),2);
        com.datastax.driver.dse.geometry.Point p0 = pointfunc.apply(value);
        com.datastax.driver.dse.geometry.Point p1 = pointfunc.apply(value+1);

        com.datastax.driver.dse.geometry.Point[] points = new com.datastax.driver.dse.geometry.Point[linelen-2];

        for (int i = 2; i < linelen; i++) {
            points[i-2]=pointfunc.apply(value+i);
        }
        return new com.datastax.driver.dse.geometry.LineString(p0,p1,points);
    }
}
