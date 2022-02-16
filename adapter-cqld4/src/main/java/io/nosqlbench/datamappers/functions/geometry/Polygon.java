package io.nosqlbench.datamappers.functions.geometry;

import com.datastax.dse.driver.internal.core.data.geometry.DefaultPolygon;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

/**
 * Create a com.datastax.driver.dse.geometry.Polygon
 */
@SuppressWarnings("ALL")
@ThreadSafeMapper
@Categories({Category.objects})
public class Polygon implements LongFunction<com.datastax.dse.driver.api.core.data.geometry.Polygon > {

    private final LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point> pointfunc;
    private final LongToIntFunction lenfunc;

    public Polygon(LongToIntFunction lenfunc, LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point> pointfunc) {
        this.pointfunc = pointfunc;
        this.lenfunc = lenfunc;
    }

    public Polygon(LongToIntFunction lenfunc, LongToDoubleFunction xfunc, LongToDoubleFunction yfunc) {
        this.lenfunc = lenfunc;
        this.pointfunc=new Point(xfunc,yfunc);
    }

    public Polygon(int len, LongFunction<com.datastax.dse.driver.api.core.data.geometry.Point> pointfunc) {
        this.lenfunc = (i) -> len;
        this.pointfunc = pointfunc;
    }

    @Override
    public com.datastax.dse.driver.api.core.data.geometry.Polygon apply(long value) {
        int linelen = Math.max(lenfunc.applyAsInt(value),3);
        com.datastax.dse.driver.api.core.data.geometry.Point p0 = pointfunc.apply(value);
        com.datastax.dse.driver.api.core.data.geometry.Point p1 = pointfunc.apply(value+1);
        com.datastax.dse.driver.api.core.data.geometry.Point p2 = pointfunc.apply(value+2);
        com.datastax.dse.driver.api.core.data.geometry.Point[] points =
            new com.datastax.dse.driver.api.core.data.geometry.Point[linelen-3];

        for (int i = 3; i < linelen; i++) {
            points[i-3]=pointfunc.apply(value+i);
        }
        return new DefaultPolygon(p0,p1,p2,points);
    }
}
